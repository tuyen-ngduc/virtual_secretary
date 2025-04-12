package com.virtualsecretary.virtual_secretary.entity;

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
@Table(name = "meeting_message")
public class MeetingMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    Meeting meeting;
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    Member member;

    @Lob
    @Column(nullable = false)
    String content;

    @Column(nullable = false)
    LocalDateTime createdTime;
}
