package com.virtualsecretary.virtual_secretary.dto.response;

import com.virtualsecretary.virtual_secretary.enums.MessageType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MeetingMessageResponse {
    long chatId;
    String sender;
    String receive;
    MessageType type;
    MultipartFile file;
    String fileName;
    String message;
    String timestamp;
}
