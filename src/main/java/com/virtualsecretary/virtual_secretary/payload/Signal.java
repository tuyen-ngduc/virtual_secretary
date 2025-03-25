package com.virtualsecretary.virtual_secretary.payload;
import com.virtualsecretary.virtual_secretary.dto.response.UserJoinMeetingResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Signal {
    String type;
    String from;
    String to;
    UserJoinMeetingResponse member;
    Object payload;


}
