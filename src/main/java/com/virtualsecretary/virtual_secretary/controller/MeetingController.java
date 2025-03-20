package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.request.JoinRequest;
import com.virtualsecretary.virtual_secretary.dto.request.MeetingCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.*;
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
    public void joinRoom(@Payload JoinRequest request, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {

            String employeeCode = principal.getName();
            UserJoinMeetingResponse user = memberService.getUserJoinInfo(employeeCode, request.getMeetingCode());
            String socketId = headerAccessor.getSessionId();


            Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("userId", user.getEmployeeCode());
            headerAccessor.getSessionAttributes().put("socketId", socketId);
            headerAccessor.getSessionAttributes().put("meetingCode", request.getMeetingCode());


            Map<String, Object> userJoinedMessage = new HashMap<>();
            userJoinedMessage.put("user", user);
            userJoinedMessage.put("socketId", socketId);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getMeetingCode(),
                    userJoinedMessage
            );


            JoinResponse joinResponse = new JoinResponse();
            joinResponse.setMeetingCode(request.getMeetingCode());
            joinResponse.setIsTurnOnCamera(request.getIsTurnOnCamera());

            messagingTemplate.convertAndSendToUser(
                    employeeCode,
                    "/queue/join",
                    joinResponse
            );

            log.info("User {} joined meeting {}", user.getEmployeeCode(), request.getMeetingCode());
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
                memberService.deactivateMemberByEmployeeCode(employeeCode);

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

}
