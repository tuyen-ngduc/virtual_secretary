package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.dto.request.MeetingMessageRequest;
import com.virtualsecretary.virtual_secretary.dto.response.MeetingMessageResponse;
import com.virtualsecretary.virtual_secretary.entity.MeetingMessage;
import com.virtualsecretary.virtual_secretary.entity.Member;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.repository.MeetingMessageRepository;
import com.virtualsecretary.virtual_secretary.repository.MemberRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class MeetingMessageService {
    MeetingMessageRepository meetingMessageRepository;
    MemberRepository memberRepository;

    public MeetingMessage saveMessage(MeetingMessageRequest dto) throws IOException {
        Member sender = memberRepository.findByUser_EmployeeCodeAndMeeting_MeetingCode(dto.getSender(), dto.getReceive())
                .orElseThrow(() -> new IndicateException(ErrorCode.SENDER_NOT_EXISTED));

        MeetingMessage msg = new MeetingMessage();
        msg.setChatId(dto.getChatId());
        msg.setSender(sender);
        msg.setReceive(dto.getReceive());
        msg.setType(dto.getType());
        msg.setMessage(dto.getMessage());
        msg.setTimestamp(dto.getTimestamp());
        msg.setFileName(dto.getFileName());

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            msg.setFile(dto.getFile().getBytes());
        }


        return meetingMessageRepository.save(msg);
    }

    public List<MeetingMessageResponse> getMessagesByMeetingCode(String meetingCode) {
        List<MeetingMessage> messages = meetingMessageRepository.findByReceive(meetingCode);

        return messages.stream().map(msg -> {
            MeetingMessageResponse res = new MeetingMessageResponse();
            res.setChatId(msg.getChatId());
            res.setSender(msg.getSender().getUser().getEmployeeCode());
            res.setReceive(msg.getReceive());
            res.setType(msg.getType());
            res.setFileName(msg.getFileName());
            res.setMessage(msg.getMessage());
            res.setTimestamp(msg.getTimestamp());
            return res;
        }).collect(Collectors.toList());
    }

    public MeetingMessageResponse getMessageByChatId(Long chatId) {
        MeetingMessage msg = meetingMessageRepository.findByChatId(chatId)
                .orElseThrow(() -> new IndicateException(ErrorCode.MESSAGE_NOT_EXISTED));

        MeetingMessageResponse res = new MeetingMessageResponse();
        res.setChatId(msg.getChatId());
        res.setSender(msg.getSender().getUser().getEmployeeCode());
        res.setReceive(msg.getReceive());
        res.setType(msg.getType());
        res.setFileName(msg.getFileName());
        res.setMessage(msg.getMessage());
        res.setTimestamp(msg.getTimestamp());
        return res;
    }




}
