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

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    Room room;
    @Column(nullable = false)
    String name;
    @Column(nullable = false, unique = true)
    String rememberCode;
    @Column(nullable = false)
    LocalDateTime startTime;
    LocalDateTime endTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MeetingStatus status;


}

