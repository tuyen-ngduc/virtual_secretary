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
@Table(name = "meeting_record")
public class MeetingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    Meeting meeting;
    @Lob
    @Column(nullable = false)
    String transcript;
    @Column(nullable = false, unique = true)
    String audioFileUrl;
    @Column(nullable = false)
    LocalDateTime createdTime;
}
