package com.virtualsecretary.virtual_secretary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMeetingRequest {
    @NotNull(message = "Id cannot be null")
    long id;
    @NotBlank(message = "Meeting name cannot be blank")
    @Size(min = 3, max = 50, message = "Meeting code must be between 3 and 50 characters")
    String meetingCode;

    @NotNull(message = "Room ID cannot be null")
    Long roomId;

    long departmentId;

    @NotBlank(message = "Meeting name cannot be blank")
    @Size(min = 3, max = 100, message = "Meeting name must be between 3 and 100 characters")
    String name;

    @NotNull(message = "Start time cannot be null")
    LocalDateTime startTime;
    @NotNull(message = "Start time cannot be null")
    LocalDateTime endTime;
}
