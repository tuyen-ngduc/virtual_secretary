package com.virtualsecretary.virtual_secretary.entity;

import com.virtualsecretary.virtual_secretary.enums.MeetingRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;
    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    Meeting meeting;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MeetingRole meetingRole;

    @Column(nullable = false)
    boolean active;

    String peerId;
}