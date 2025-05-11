package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.request.NotificationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.dto.response.NotificationResponse;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

     NotificationService notificationService;

    @PostMapping
    public ApiResponse<Void> postNotification(@RequestBody NotificationRequest dto) {
            notificationService.saveNotification(dto);
            return ApiResponse.<Void>builder()
                    .code(200)
                    .message("Notification saved")
                    .build();

    }
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getAllByMeetingCode(
            @RequestParam("employeeCode") String employeeCode) {
        List<NotificationResponse> result = notificationService.getAllNotificationsByEmployeeCode(employeeCode);
        return ApiResponse.<List<NotificationResponse>>builder()
                .code(200)
                .result(result)
                .build();
    }




    @PutMapping("/read/{employeeCode}")
    public ApiResponse<Void> markAllAsRead(@PathVariable String employeeCode) {
        notificationService.markAllNotificationsAsReadByReceiver(employeeCode);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Marked all notifications as read for " + employeeCode)
                .build();
    }


}

