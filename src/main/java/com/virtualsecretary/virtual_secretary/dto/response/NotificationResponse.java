package com.virtualsecretary.virtual_secretary.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    String notificationId;
    String sender;
    String content;
    String timestamp;
    boolean isRead;
    String receive;
}

