package com.virtualsecretary.virtual_secretary.payload;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Signal {
    String type;  // "offer", "answer", "ice-candidate"
    String meetingCode;  // Xác định cuộc họp nào
    String senderId;
    String receiverId;
    String sdp;  // Chỉ dùng cho offer/answer
    String candidate;  // Chỉ dùng cho ICE Candidate
    Integer sdpMLineIndex;
    String sdpMid;
    boolean isC, isM, isS;
    String peerId;

}
