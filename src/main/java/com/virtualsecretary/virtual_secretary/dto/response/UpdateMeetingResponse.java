package com.virtualsecretary.virtual_secretary.dto.response;

import com.virtualsecretary.virtual_secretary.enums.MeetingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMeetingResponse {
    long id;
    String meetingCode;
    long roomId;
    long departmentId;
    String name;
    LocalDateTime startTime;
    LocalDateTime endTime;
    MeetingStatus status;
}
