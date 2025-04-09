package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.dto.request.MeetingCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.request.UpdateMeetingRequest;
import com.virtualsecretary.virtual_secretary.dto.response.*;
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
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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

    public UpdateMeetingResponse updateMeeting(UpdateMeetingRequest request) {
        Meeting meeting = meetingRepository.findById(request.getId())
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IndicateException(ErrorCode.ROOM_NOT_EXISTED));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IndicateException(ErrorCode.DEPARTMENT_NOT_EXISTED));


        meeting.setMeetingCode(request.getMeetingCode());
        meeting.setRoom(room);
        meeting.setDepartment(department);
        meeting.setName(request.getName());
        meeting.setStartTime(request.getStartTime());
        meeting.setEndTime(request.getEndTime());

        meetingRepository.save(meeting);

        return meetingMapper.toUpdateMeetingResponse(meeting);
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
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

    public void deleteMeeting(long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        meetingRepository.delete(meeting);
    }

    public WeeklyMeetingResponse getMeetingsByWeek() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        LocalDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);

        List<Meeting> meetings = meetingRepository.findByStartTimeBetween(startOfWeek, endOfWeek);

        Map<String, List<MeetingCreationResponse>> result = new LinkedHashMap<>();


        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            result.put(dayName, new ArrayList<>());
        }

        for (Meeting meeting : meetings) {
            DayOfWeek dayOfWeek = meeting.getStartTime().getDayOfWeek(); // Ví dụ: MONDAY
            String dayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            result.get(dayName).add(meetingMapper.toMeetingCreationResponse(meeting));
        }

        return new WeeklyMeetingResponse(result);
    }

    public MonthlyMeetingResponse getMeetingsByMonth(int month, int year) {
        YearMonth selectedMonth = YearMonth.of(year, month);

        // Tính ngày bắt đầu từ tuần chứa ngày 1 -> đến hết tuần chứa ngày cuối tháng
        LocalDateTime from = selectedMonth.atDay(1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        LocalDateTime to = selectedMonth.atEndOfMonth()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(LocalTime.MAX);

        List<Meeting> meetings = meetingRepository.findByStartTimeBetween(from, to);

        // Chia theo tuần -> ngày
        Map<String, Map<String, List<MeetingCreationResponse>>> result = new LinkedHashMap<>();

        LocalDate start = from.toLocalDate();
        LocalDate end = to.toLocalDate();

        int weekIndex = 1;
        for (LocalDate weekStart = start; !weekStart.isAfter(end); weekStart = weekStart.plusWeeks(1)) {
            String weekLabel = "Week " + weekIndex++;

            Map<String, List<MeetingCreationResponse>> daysInWeek = new LinkedHashMap<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                String dayName = day.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                daysInWeek.put(dayName, new ArrayList<>());
            }

            result.put(weekLabel, daysInWeek);
        }

        for (Meeting meeting : meetings) {
            LocalDate meetingDate = meeting.getStartTime().toLocalDate();
            int daysFromStart = (int) ChronoUnit.DAYS.between(start, meetingDate);
            int weekNumber = (daysFromStart / 7) + 1;
            String weekLabel = "Week " + weekNumber;

            String dayName = meetingDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            if (result.containsKey(weekLabel)) {
                result.get(weekLabel).get(dayName)
                        .add(meetingMapper.toMeetingCreationResponse(meeting));
            }
        }

        return new MonthlyMeetingResponse(result);
    }



}


