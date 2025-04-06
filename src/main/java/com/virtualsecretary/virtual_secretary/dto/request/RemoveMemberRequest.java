package com.virtualsecretary.virtual_secretary.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RemoveMemberRequest {
    @Min(value = 1, message = "User ID must be greater than 0")
    long userId;
    @Min(value = 1, message = "Meeting ID must be greater than 0")
    long meetingId;
}
