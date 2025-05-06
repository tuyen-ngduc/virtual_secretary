package com.virtualsecretary.virtual_secretary.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class PostponeMeetingRequest {
    @NotNull
    LocalDateTime newStartTime;

    @NotNull
    LocalDateTime newEndTime;
}
