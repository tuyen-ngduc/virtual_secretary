package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.dto.request.MeetingCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.MeetingCreationResponse;
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

import java.time.LocalDateTime;
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
    MemberMapper memberMapper;
    UserRepository userRepository;

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
        meeting.setStatus(getMeetingStatus(meeting));
        meeting.setDepartment(department);

        meetingRepository.save(meeting);
        return meetingMapper.toMeetingCreationResponse(meeting);
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    private MeetingStatus getMeetingStatus(Meeting meeting) {
        var now = LocalDateTime.now();
        var startTime = meeting.getStartTime();
        var endTime = startTime.plusHours(4);
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

}
