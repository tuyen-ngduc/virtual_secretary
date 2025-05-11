package com.virtualsecretary.virtual_secretary.repository;

import com.virtualsecretary.virtual_secretary.entity.MeetingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingMessageRepository extends JpaRepository<MeetingMessage, Long> {
    List<MeetingMessage> findByReceive(String meetingCode);
    Optional<MeetingMessage> findByChatId(long chatId);
}
