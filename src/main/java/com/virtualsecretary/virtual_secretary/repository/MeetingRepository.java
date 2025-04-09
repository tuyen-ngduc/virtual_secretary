package com.virtualsecretary.virtual_secretary.repository;

import com.virtualsecretary.virtual_secretary.entity.Meeting;
import com.virtualsecretary.virtual_secretary.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    @Modifying
    @Query("UPDATE Meeting m SET m.department = NULL WHERE m.department.id = :departmentId")
    void updateDepartmentInMeetingToNull(@Param("departmentId") long departmentId);

    @Query("SELECT m FROM Meeting m WHERE m.startTime BETWEEN :start AND :end")
    List<Meeting> findByStartTimeBetween(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Modifying
    @Query("UPDATE Meeting m SET m.room = NULL WHERE m.room.id = :roomId")
    void updateRoomInMeetingToNull(@Param("roomId") long roomId);
    boolean existsByMeetingCode(String meetingCode);
    Optional<Meeting> findByMeetingCode(String meetingCode);

    @Query("SELECT m FROM Meeting m JOIN Member mem ON m.id = mem.meeting.id WHERE mem.user.id = :userId")
    List<Meeting> findMeetingsByUserId(@Param("userId") Long userId);



}
