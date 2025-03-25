package com.virtualsecretary.virtual_secretary.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JoinRequest {
    private String meetingCode;
    private String peerId;
}
