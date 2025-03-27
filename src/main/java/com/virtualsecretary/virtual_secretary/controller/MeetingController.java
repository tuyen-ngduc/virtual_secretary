package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.request.JoinRequest;
import com.virtualsecretary.virtual_secretary.dto.request.MeetingCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.*;
import com.virtualsecretary.virtual_secretary.payload.Notification;
import com.virtualsecretary.virtual_secretary.payload.Signal;
import com.virtualsecretary.virtual_secretary.service.MeetingParticipantManager;
import com.virtualsecretary.virtual_secretary.service.MeetingService;
import com.virtualsecretary.virtual_secretary.service.MemberService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MeetingController {
    MeetingService meetingService;
    SimpMessagingTemplate messagingTemplate;
    MemberService memberService;
    MeetingParticipantManager participantManager;


    @GetMapping
    public ApiResponse<List<MeetingCreationResponse>> getAllMeetings() {
        return ApiResponse.<List<MeetingCreationResponse>>builder()
                .code(200)
                .result(meetingService.getAllMeetings()).build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<MeetingCreationResponse>> getMeetingById(@PathVariable long userId) {
        return ApiResponse.<List<MeetingCreationResponse>>builder()
                .code(200)
                .result(meetingService.getMyMeetings(userId))
                .build();
    }

    @PostMapping("/create")
    public ApiResponse<MeetingCreationResponse> createMeeting(@RequestBody @Valid MeetingCreationRequest request) {
        return ApiResponse.<MeetingCreationResponse>builder()
                .code(200)
                .message("Meeting created")
                .result(meetingService.createMeeting(request))
                .build();
    }


    @MessageMapping("/join")
    public void joinMeeting(@Payload JoinRequest request, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String employeeCode = principal.getName();
            log.info("Joining member {} with meeting code {}", employeeCode, request.getMeetingCode());

            UserJoinMeetingResponse member = memberService.getUserJoinInfo(employeeCode, request.getMeetingCode());
            String sessionId = headerAccessor.getSessionId();
            String peerId = request.getPeerId();

            Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("employeeCode", member.getEmployeeCode());
            headerAccessor.getSessionAttributes().put("sessionId", sessionId);
            headerAccessor.getSessionAttributes().put("meetingCode", request.getMeetingCode());
            headerAccessor.getSessionAttributes().put("peerId", peerId);

            memberService.validateAndActivateMember(employeeCode, request.getMeetingCode());

            Signal signal = Signal.builder()
                    .type("user-joined")
                    .from(peerId)
                    .to("all")
                    .member(member)
                    .payload(Map.of("member", member, "peerId", peerId))
                    .build();
            participantManager.addParticipant(request.getMeetingCode(), signal);

            messagingTemplate.convertAndSend("/topic/room/" + request.getMeetingCode(), signal);

            log.info("User {} joined meeting {} with PeerId {}", member.getEmployeeCode(), request.getMeetingCode(), peerId);

        } catch (Exception e) {
            log.error("Error during join room process", e);
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    Map.of("message", "Error joining meeting: " + e.getMessage())
            );
        }
    }


    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String employeeCode = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("employeeCode");
        String meetingCode = (String) headerAccessor.getSessionAttributes().get("meetingCode");
        String peerId = (String) headerAccessor.getSessionAttributes().get("peerId");

        if (employeeCode != null) {
            log.info("User {} disconnected from meeting {}", employeeCode, meetingCode);

            try {
                memberService.deactivateMemberByEmployeeCode(employeeCode, meetingCode);
                memberService.updatePeerId(employeeCode, meetingCode, null);
                if (peerId != null) {
                    participantManager.removeParticipant(meetingCode, peerId);
                    log.info("Removed participant {} from meeting {}", peerId, meetingCode);
                }

                if (meetingCode != null) {
                    messagingTemplate.convertAndSend("/topic/meeting/" + meetingCode,
                            new Notification("User " + employeeCode + " đã rời khỏi cuộc họp"));

                    List<UserJoinMeetingResponse> activeMembers = memberService.getActiveMembers(meetingCode);
                    messagingTemplate.convertAndSend("/topic/meeting/" + meetingCode + "/members", activeMembers);
                }
            } catch (Exception e) {
                log.error("Error during disconnect handling", e);
            }
        }
    }

    @MessageMapping("/participants")
    public void getParticipants(@Payload JoinRequest request, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String meetingCode = request.getMeetingCode();
        String employeeCode = principal.getName();
        String peerId = request.getPeerId();
        UserJoinMeetingResponse member = memberService.getUserJoinInfo(employeeCode, request.getMeetingCode());
        List<Signal> participants = participantManager.getParticipants(meetingCode, peerId);
        Signal signal = Signal.builder()
                .type("meeting-users")
                .from("server")
                .to(peerId)
                .member(member)
                .payload(Map.of("members", participants))
                .build();
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/room/" + meetingCode, signal);
        log.info("Sending participant list to {}: {}", peerId, participants);

    }

    @MessageMapping("/signal/{meetingCode}")
    public void signal(@DestinationVariable String meetingCode, @Payload Signal signal) {
        messagingTemplate.convertAndSend("/topic/room/" + meetingCode, signal);
    }

    @PostMapping("/{meetingCode}/upload-audio")
    public ApiResponse<String> uploadAudio(
            @PathVariable String meetingCode,
            @RequestParam("file") MultipartFile file) {

        return ApiResponse.<String>builder()
                .code(200)
                .message("File saved")
                .result(meetingService.saveAudio(meetingCode, file))
                .build();
    }


}
