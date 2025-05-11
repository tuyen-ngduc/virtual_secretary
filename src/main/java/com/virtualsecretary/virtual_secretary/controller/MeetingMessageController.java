package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.request.MeetingMessageRequest;
import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.dto.response.MeetingMessageResponse;
import com.virtualsecretary.virtual_secretary.entity.MeetingMessage;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.service.MeetingMessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MeetingMessageController {
    MeetingMessageService meetingMessageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> postMessage(@ModelAttribute MeetingMessageRequest dto) {
        try {
            MeetingMessage saved = meetingMessageService.saveMessage(dto);
            return ApiResponse.<Void>builder()
                    .code(200).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IndicateException(ErrorCode.UNCATEGORIZED_EXCEPTION);

        }
    }

    @GetMapping("/all")
    public ApiResponse<List<MeetingMessageResponse>> getMessagesByMeetingCode(@RequestParam String meetingCode) {
        List<MeetingMessageResponse> messages = meetingMessageService.getMessagesByMeetingCode(meetingCode);
        return ApiResponse.<List<MeetingMessageResponse>>builder()
                .code(200)
                .result(messages)
                .build();
    }

    @GetMapping("/{chatId}")
    public ApiResponse<MeetingMessageResponse> getMessageById(@PathVariable Long chatId) {
        MeetingMessageResponse response = meetingMessageService.getMessageByChatId(chatId);
        return ApiResponse.<MeetingMessageResponse>builder()
                .code(200)
                .result(response)
                .build();
    }


}
