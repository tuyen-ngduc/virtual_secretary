package com.virtualsecretary.virtual_secretary.dto.response;

import com.virtualsecretary.virtual_secretary.enums.MeetingRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemberResponse {
    long id;
    UserResponse user;
    MeetingCreationResponse meeting;
    MeetingRole meetingRole;
    String active;

}
