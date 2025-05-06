package com.virtualsecretary.virtual_secretary.entity;

import com.virtualsecretary.virtual_secretary.enums.MeetingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "meeting")
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
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
    LocalDateTime endTime;
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.REMOVE)
    List<Member> members;

    boolean isCancelled = false;
    boolean isPostponed = false;



    public MeetingStatus getMeetingStatus() {

        if (isCancelled) return MeetingStatus.CANCELLED;
        if (isPostponed) return MeetingStatus.POSTPONED;

        LocalDateTime now = LocalDateTime.now();
        if (startTime.isAfter(now)) {
            if (now.isBefore(startTime.minusHours(2))) {
                return MeetingStatus.NOT_STARTED;
            }
            return MeetingStatus.UPCOMING;
        } else if (now.isAfter(endTime)) {
            return MeetingStatus.ENDED;
        } else if (startTime.isBefore(now) && now.isBefore(endTime)) {
            return MeetingStatus.ONGOING;
        }

        return MeetingStatus.UNKNOWN;
    }




}

