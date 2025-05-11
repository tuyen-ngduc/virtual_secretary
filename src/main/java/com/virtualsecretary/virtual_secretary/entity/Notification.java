package com.virtualsecretary.virtual_secretary.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String notificationId;
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    User sender;
    @Column(nullable = false)
    String receive;
    @Column(nullable = false)
    String content;
    @Column(nullable = false)
    String timestamp;
    @Column(nullable = false)
    boolean isRead;


}
