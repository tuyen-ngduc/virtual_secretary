package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.dto.request.MeetingCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.MeetingCreationResponse;
import com.virtualsecretary.virtual_secretary.dto.response.UserJoinMeetingResponse;
import com.virtualsecretary.virtual_secretary.entity.*;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.enums.MeetingStatus;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.mapper.MeetingMapper;
import com.virtualsecretary.virtual_secretary.mapper.MemberMapper;
import com.virtualsecretary.virtual_secretary.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class MeetingService {
    MeetingRepository meetingRepository;
    RoomRepository roomRepository;
    MemberRepository memberRepository;
    DepartmentRepository departmentRepository;
    MeetingMapper meetingMapper;

    @PreAuthorize("hasRole('SECRETARY')")
    public MeetingCreationResponse createMeeting(MeetingCreationRequest request) {
        log.info("Starting to create meeting with request: {}", request);

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IndicateException(ErrorCode.ROOM_NOT_EXISTED));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IndicateException(ErrorCode.DEPARTMENT_NOT_EXISTED));

        if (meetingRepository.existsByMeetingCode(request.getMeetingCode())) {
            throw new IndicateException(ErrorCode.MEETING_EXISTED);
        }

        Meeting meeting = meetingMapper.toMeeting(request);
        meeting.setRoom(room);
        meeting.setDepartment(department);

        meetingRepository.save(meeting);
        createMeetingDirectories(request.getMeetingCode());

        return meetingMapper.toMeetingCreationResponse(meeting);
    }

    private void createMeetingDirectories(String meetingCode) {
        try {
            Path audioPath = Paths.get("audio", meetingCode);
            Path sttPath = Paths.get("stt", meetingCode);

            Files.createDirectories(audioPath);
            Files.createDirectories(sttPath);

            log.info("Created directories: {} and {}", audioPath, sttPath);
        } catch (Exception e) {
            log.error("Failed to create directories for meeting {}: {}", meetingCode, e.getMessage());
            throw new RuntimeException("Error creating meeting directories", e);
        }
    }

    public List<MeetingCreationResponse> getAllMeetings() {
        return meetingRepository.findAll().stream().map(meetingMapper::toMeetingCreationResponse).toList();
    }

    public List<MeetingCreationResponse> getMyMeetings(long userId) {

        List<Member> members = memberRepository.findByUserId(userId);

        return members.stream()
                .map(Member::getMeeting)
                .map(meetingMapper::toMeetingCreationResponse)
                .collect(Collectors.toList());
    }

    public String saveAudio(String meetingCode, MultipartFile file) {
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        Path audioDirectory = Paths.get("audio", meetingCode);
        try {
            Files.createDirectories(audioDirectory);
        } catch (IOException e) {
            throw new IndicateException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = audioDirectory.resolve(fileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file audio", e);
        }

        return filePath.toString();
    }


}


