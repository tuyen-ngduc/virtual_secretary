package com.virtualsecretary.virtual_secretary.dto.request;

import com.virtualsecretary.virtual_secretary.enums.MeetingRole;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddMemberRequest {
        @Min(value = 1, message = "User ID must be greater than 0")
        long userId;
        @Min(value = 1, message = "Meeting ID must be greater than 0")
        long meetingId;
        @NotNull(message = "Meeting role must not be null")
        MeetingRole meetingRole;
        boolean forceAdd = false;

}
