package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.request.JoinRoomRequest;
import com.virtualsecretary.virtual_secretary.dto.request.MeetingCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.dto.response.MeetingCreationResponse;
import com.virtualsecretary.virtual_secretary.dto.response.MemberResponse;
import com.virtualsecretary.virtual_secretary.dto.response.Notification;
import com.virtualsecretary.virtual_secretary.service.MeetingService;
import com.virtualsecretary.virtual_secretary.service.MemberService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MeetingController {
    MeetingService meetingService;
    SimpMessagingTemplate messagingTemplate;
    MemberService memberService;

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
    public void joinRoom(@Payload JoinRoomRequest request, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String employeeCode = principal.getName();
            log.info("User {} is joining meeting with code {}", employeeCode, request.getMeetingCode());
            Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("meetingCode", request.getMeetingCode());
            headerAccessor.getSessionAttributes().put("employeeCode", employeeCode);
            memberService.validateAndActivateMember(employeeCode, request.getMeetingCode());
            messagingTemplate.convertAndSend("/topic/meeting/" + request.getMeetingCode(),
                    new Notification("User " + employeeCode + " đã tham gia cuộc họp"));
            List<MemberResponse> activeMembers = memberService.getActiveMembers(request.getMeetingCode());
            messagingTemplate.convertAndSendToUser(employeeCode, "/queue/active-members", activeMembers);
            messagingTemplate.convertAndSend("/topic/meeting/" + request.getMeetingCode() + "/members", activeMembers);
        } catch (Exception e) {
            log.error("Error during join room process", e);
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors",
                    new Notification("Error joining meeting: " + e.getMessage()));
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String employeeCode = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("employeeCode");
        String meetingCode = (String) headerAccessor.getSessionAttributes().get("meetingCode");

        if (employeeCode != null) {
            log.info("User {} disconnected from meeting {}", employeeCode, meetingCode);

            try {
                memberService.deactivateMemberByEmployeeCode(employeeCode);

                if (meetingCode != null) {
                    messagingTemplate.convertAndSend("/topic/meeting/" + meetingCode,
                            new Notification("User " + employeeCode + " đã rời khỏi cuộc họp"));

                    List<MemberResponse> activeMembers = memberService.getActiveMembers(meetingCode);
                    messagingTemplate.convertAndSend("/topic/meeting/" + meetingCode + "/members", activeMembers);
                }
            } catch (Exception e) {
                log.error("Error during disconnect handling", e);
            }
        }
    }

}
