package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.entity.Meeting;
import com.virtualsecretary.virtual_secretary.entity.Member;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.repository.MeetingRepository;
import com.virtualsecretary.virtual_secretary.repository.MemberRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AudioService {

    MeetingRepository meetingRepository;
    MemberRepository memberRepository;
    RestTemplate restTemplate;


    public String saveAudio(String meetingCode, MultipartFile file) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        Member member = memberRepository.findByUser_EmployeeCodeAndMeeting_MeetingCode(name, meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEMBER_NOT_EXISTED));

        Path audioDirectory = Paths.get("audio", meetingCode);
        try {
            Files.createDirectories(audioDirectory);
        } catch (IOException e) {
            throw new IndicateException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
        String extension = Optional.ofNullable(file.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse(".wav");

        // Format tên file: timestamp_tennguoi_vaitro.wav
        String formattedName = member.getUser().getName().replaceAll("\\s+", "_");
        String role = member.getMeetingRole().name();
        String fileName = String.format("%s_%s_%s%s", timestamp, formattedName, role, extension);

        Path filePath = audioDirectory.resolve(fileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file audio", e);
        }

        return filePath.toString();
    }


}


