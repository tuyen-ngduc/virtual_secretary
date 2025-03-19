package com.virtualsecretary.virtual_secretary.repository;

import com.virtualsecretary.virtual_secretary.entity.Meeting;
import com.virtualsecretary.virtual_secretary.entity.Member;
import com.virtualsecretary.virtual_secretary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUserIdAndActiveTrue(long userId);
    Optional<Member> findByUserIdAndMeetingId(Long userId, Long meetingId);
@Query("SELECT m.meeting FROM Member m WHERE m.user.id = :userId " +
        "AND m.meeting.id <> :meetingId " +
        " AND m.meeting.startTime < :endTime " +
        "AND FUNCTION('ADDTIME', m.meeting.startTime, FUNCTION('SEC_TO_TIME', m.meeting.duration * 60)) > :startTime"
)
    Optional<Member> validateMember(@Param("userId") Long userId,
                                    @Param("meetingId") Long meetingId,
                                    @Param("endTime") LocalDateTime endTime,
                                    @Param("startTime") LocalDateTime startTime);
    List<Member> findByMeetingId(Long meetingId);

    @Query("SELECT m FROM Member m WHERE m.active = true AND m.meeting.meetingCode= :meetingCode")
    List<Member> findActiveMembers(String meetingCode);
    List<Member> findByUserId(long userId);
}
