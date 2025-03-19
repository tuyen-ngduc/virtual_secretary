package com.virtualsecretary.virtual_secretary.entity;

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
@Entity
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(nullable = false, unique = true)
    String meetingCode;
    @ManyToOne
    @JoinColumn(name = "room_id")
    Room room;
    @ManyToOne
    @JoinColumn(name = "department_id")
    Department department;
    @Column(nullable = false)
    String name;
    @Column(nullable = false)
    LocalDateTime startTime;

    @Column(nullable = false)
    int duration;

    @Enumerated(EnumType.STRING)
    MeetingStatus status;

    public LocalDateTime getEndTime() {
        return startTime.plusMinutes(duration);

    }


}

