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
@Table(name = "document")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    Meeting meeting;
    @Column(nullable = false)
    String name;
    @Lob
    String content;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;
}
