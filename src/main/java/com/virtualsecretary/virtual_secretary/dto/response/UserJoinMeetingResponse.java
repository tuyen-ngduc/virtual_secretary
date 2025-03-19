package com.virtualsecretary.virtual_secretary.dto.response;

import com.virtualsecretary.virtual_secretary.enums.MeetingRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserJoinMeetingResponse {

    String employeeCode;
    String name;
    String email;
    String img;
    MeetingRole meetingRole;



}
