package com.virtualsecretary.virtual_secretary.repository;

import com.virtualsecretary.virtual_secretary.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserIdAndMeetingId(Long userId, Long meetingId);
    List<Member> findByMeetingId(Long meetingId);
    @Query("SELECT m FROM Member m WHERE m.user.id = :userId AND m.meeting.meetingCode = :meetingCode")
    Optional<Member> findByUserIdAndMeetingCode(@Param("userId") String userId, @Param("meetingCode") String meetingCode);
    List<Member> findByMeetingIdAndActiveTrue(Long meetingId);
    List<Member> findByUserIdAndActiveTrue(Long userId);
}
