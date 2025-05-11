package com.virtualsecretary.virtual_secretary.dto.request;

import com.virtualsecretary.virtual_secretary.entity.Member;
import com.virtualsecretary.virtual_secretary.enums.MessageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MeetingMessageRequest {
    long chatId;
    String sender;
    String receive;
    MessageType type;
    MultipartFile file;
    String fileName;
    String message;
    String timestamp;

}
