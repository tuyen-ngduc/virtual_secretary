package com.virtualsecretary.virtual_secretary.dto.request;

import com.virtualsecretary.virtual_secretary.entity.Department;
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
public class MeetingCreationRequest {
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


}
