package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.request.JoinRequest;
import com.virtualsecretary.virtual_secretary.dto.request.MeetingCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.*;
import com.virtualsecretary.virtual_secretary.entity.Member;
import com.virtualsecretary.virtual_secretary.payload.Notification;
import com.virtualsecretary.virtual_secretary.payload.Signal;
import com.virtualsecretary.virtual_secretary.repository.MemberRepository;
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
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
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
    MemberRepository memberRepository;


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


//    @MessageMapping("/join")
//    public void joinRoom(@Payload Signal request, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
//        try {
//
//            String employeeCode = principal.getName();
//            log.info("Joining member {} with meeting code {}", employeeCode, request.getMeetingCode());
//            UserJoinMeetingResponse member = memberService.getUserJoinInfo(employeeCode, request.getMeetingCode());
//            String peerId = request.getPeerId();
//
//            Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("employeeCode", member.getEmployeeCode());
//            headerAccessor.getSessionAttributes().put("meetingCode", request.getMeetingCode());
//            log.info("Joining member {} with meeting code {}", employeeCode, request.getMeetingCode());
//
//            memberService.validateAndActivateMember(employeeCode, request.getMeetingCode());
//
//            Map<String, Object> userJoinedMessage = new HashMap<>();
//            userJoinedMessage.put("member", member);
//            userJoinedMessage.put("peerId", peerId);
//            userJoinedMessage.put("type", )
//
//            messagingTemplate.convertAndSend("/topic/room/" + request.getMeetingCode(), userJoinedMessage);
//            log.info("Gửi cho tất cả mọi người!");
//
//            JoinResponse joinResponse = new JoinResponse();
//            joinResponse.setMeetingCode(request.getMeetingCode());
//            joinResponse.setPeerId(peerId);
//            messagingTemplate.convertAndSendToUser(employeeCode, "/queue/join", joinResponse);
//            log.info("User {} joined meeting {} with PeerId {}", member.getEmployeeCode(), request.getMeetingCode(), peerId);
//        } catch (Exception e) {
//            log.error("Error during join room process", e);
//            messagingTemplate.convertAndSendToUser(
//                    principal.getName(),
//                    "/queue/errors",
//                    Map.of("message", "Error joining meeting: " + e.getMessage())
//            );
//        }
//    }

    @MessageMapping("/join")
    public void joinRoom(@Payload JoinRequest request, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String employeeCode = principal.getName();
            log.info("Joining member {} with meeting code {}", employeeCode, request.getMeetingCode());

            UserJoinMeetingResponse member = memberService.getUserJoinInfo(employeeCode, request.getMeetingCode());
            String socketId = headerAccessor.getSessionId();
            String peerId = UUID.randomUUID().toString();

            Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("employeeCode", member.getEmployeeCode());
            headerAccessor.getSessionAttributes().put("socketId", socketId);
            headerAccessor.getSessionAttributes().put("meetingCode", request.getMeetingCode());

            memberService.validateAndActivateMember(employeeCode, request.getMeetingCode());

            Signal signal = Signal.builder()
                    .type("user-joined")
                    .from(peerId)
                    .to("all")
                    .member(member)
                    .payload(Map.of("member", member, "peerId", peerId))
                    .build();

            // Gửi thông báo đến tất cả người trong cuộc họp
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

        if (employeeCode != null) {
            log.info("User {} disconnected from meeting {}", employeeCode, meetingCode);

            try {
                memberService.deactivateMemberByEmployeeCode(employeeCode, meetingCode);
                memberService.updatePeerId(employeeCode, meetingCode, null);

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

//    @MessageMapping("/signal")
//    public void handleWebRTCSignal(@Payload Signal message, Principal principal) {
//        if (message.getReceiverId() == null || message.getSenderId() == null || message.getMeetingCode() == null) {
//            log.error("Missing meetingCode, senderId, or receiverId");
//            return;
//        }
//
//        log.info("Forwarding {} signal in meeting {} from {} to {}",
//                message.getType(), message.getMeetingCode(), message.getSenderId(), message.getReceiverId());
//
//        // Chỉ gửi nếu receiverId khác senderId
//        if (!message.getReceiverId().equals(message.getSenderId())) {
//            messagingTemplate.convertAndSendToUser(
//                    message.getReceiverId(), "/queue/signal", message
//            );
//        }
//    }



}
