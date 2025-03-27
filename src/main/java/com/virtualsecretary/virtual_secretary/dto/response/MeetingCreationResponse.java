package com.virtualsecretary.virtual_secretary.dto.response;

import com.virtualsecretary.virtual_secretary.entity.Department;
import com.virtualsecretary.virtual_secretary.entity.Meeting;
import com.virtualsecretary.virtual_secretary.entity.Room;
import com.virtualsecretary.virtual_secretary.enums.MeetingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MeetingCreationResponse {
    long id;
    String meetingCode;
    Room room;
    Department department;
    String name;
    LocalDateTime startTime;
    LocalDateTime endTime;
    MeetingStatus status;


}
