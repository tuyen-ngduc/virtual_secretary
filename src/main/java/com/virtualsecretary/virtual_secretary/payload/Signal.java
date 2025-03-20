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
    String meetingCode;
    String to;
    Map<String, Object> offer;
    boolean isC, isM, isS;

}
