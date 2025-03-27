package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.dto.request.AddMemberRequest;
import com.virtualsecretary.virtual_secretary.dto.response.MemberResponse;
import com.virtualsecretary.virtual_secretary.dto.response.UserJoinMeetingResponse;
import com.virtualsecretary.virtual_secretary.entity.Meeting;
import com.virtualsecretary.virtual_secretary.entity.Member;
import com.virtualsecretary.virtual_secretary.entity.User;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.mapper.MemberMapper;
import com.virtualsecretary.virtual_secretary.mapper.UserMapper;
import com.virtualsecretary.virtual_secretary.repository.MeetingRepository;
import com.virtualsecretary.virtual_secretary.repository.MemberRepository;
import com.virtualsecretary.virtual_secretary.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class MemberService {
    MemberRepository memberRepository;
    UserRepository userRepository;
    MeetingRepository meetingRepository;
    MemberMapper memberMapper;

    @PreAuthorize("hasRole('SECRETARY')")
    public MemberResponse addMemberToMeeting(AddMemberRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IndicateException(ErrorCode.USER_NOT_EXISTED));

        Meeting meeting = meetingRepository.findById(request.getMeetingId())
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        List<Meeting> existingMeetings = meetingRepository.findMeetingsByUserId(request.getUserId());

        boolean isOverlapping = existingMeetings.stream().anyMatch(existingMeeting ->
                isTimeOverlapping(meeting.getStartTime(), meeting.getEndTime(),
                        existingMeeting.getStartTime(), existingMeeting.getEndTime()));

        Member member = new Member();
        member.setUser(user);
        member.setMeeting(meeting);
        member.setMeetingRole(request.getMeetingRole());
        memberRepository.save(member);


        MemberResponse response = memberMapper.toMemberResponse(member);


        if (isOverlapping) {
            response.setWarning("⚠️ Cảnh báo: Nhân viên " +user.getName() + " đã có cuộc họp khác trong khoảng thời gian này!");
        }

        return response;
    }

    private boolean isTimeOverlapping(LocalDateTime newStart, LocalDateTime newEnd,
                                      LocalDateTime existingStart, LocalDateTime existingEnd) {
        return (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart));


    }

    public List<MemberResponse> getMembersByMeetingId(Long meetingId) {
        List<Member> members = memberRepository.findByMeetingId(meetingId);
        return members.stream()
                .map(memberMapper::toMemberResponse)
                .collect(Collectors.toList());
    }

    public void validateAndActivateMember(String employeeCode, String meetingCode) {
        Member member = memberRepository.findByUser_EmployeeCodeAndMeeting_MeetingCode(employeeCode, meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEMBER_NOT_EXISTED));

        member.setActive(true);
        memberRepository.save(member);
    }
    public List<UserJoinMeetingResponse> getActiveMembers(String meetingCode) {
     try {
         List<Member> members = memberRepository.findActiveMembers(meetingCode);
         return members.stream().map(member -> {
             User u = member.getUser();
             return UserJoinMeetingResponse.builder()
                     .img(u.getImg())
                     .name(u.getName())
                     .employeeCode(u.getEmployeeCode())
                     .email(u.getEmail())
                     .build();
         }).toList();
     }catch(Exception e) {
        throw new IndicateException(ErrorCode.MEETING_NOT_EXISTED);
     }

    }
    public void deactivateMemberByEmployeeCode(String employeeCode, String meetingCode) {
        Member member = memberRepository.findByUser_EmployeeCodeAndMeeting_MeetingCode(employeeCode, meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEMBER_NOT_EXISTED));
        member.setActive(false);
        memberRepository.save(member);
    }

    public UserJoinMeetingResponse getUserJoinInfo(String employeeCode, String meetingCode) {
        Member member = memberRepository
                .findByUser_EmployeeCodeAndMeeting_MeetingCode(employeeCode, meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEMBER_NOT_EXISTED));

        User user = member.getUser();
        return new UserJoinMeetingResponse(
                user.getEmployeeCode(),
                user.getName(),
                user.getEmail(),
                user.getImg(),
                member.getMeetingRole()
        );
    }


    public void updatePeerId(String employeeCode, String meetingCode, String peerId) {
        Member member = memberRepository.findByUser_EmployeeCodeAndMeeting_MeetingCode(employeeCode, meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEMBER_NOT_EXISTED));;
        if (member != null) {
            member.setPeerId(peerId);
            memberRepository.save(member);
        }
    }






}
