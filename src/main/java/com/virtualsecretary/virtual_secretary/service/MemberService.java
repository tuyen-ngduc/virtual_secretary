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
    UserMapper userMapper;

    @PreAuthorize("hasRole('ROLE_SECRETARY')")
    public MemberResponse addMemberToMeeting(AddMemberRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IndicateException(ErrorCode.USER_NOT_EXISTED));

        Meeting meeting = meetingRepository.findById(request.getMeetingId())
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_EXISTED));


        Optional<Member> existing = memberRepository.validateMember(user.getId(), meeting.getId(), meeting.getEndTime(), meeting.getStartTime());
        if (existing.isPresent()) {
            throw new IndicateException(ErrorCode.MEMBER_EXISTED);
        }
        Member member = new Member();
        member.setUser(user);
        member.setMeeting(meeting);
        member.setMeetingRole(request.getMeetingRole());
        memberRepository.save(member);
        return memberMapper.toMemberResponse(member);
    }

    public List<MemberResponse> getMembersByMeetingId(Long meetingId) {
        List<Member> members = memberRepository.findByMeetingId(meetingId);
        return members.stream()
                .map(memberMapper::toMemberResponse)
                .collect(Collectors.toList());
    }

    public void validateAndActivateMember(String employeeCode, String meetingCode) {
        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.USER_NOT_EXISTED));

        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        Member member = memberRepository.findByUserIdAndMeetingId(user.getId(), meeting.getId())
                .orElseThrow(() -> new IndicateException(ErrorCode.UNAUTHORIZED));

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
    public void deactivateMemberByEmployeeCode(String employeeCode) {
        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.USER_NOT_EXISTED));

        List<Member> members = memberRepository.findByUserIdAndActiveTrue(user.getId());
        for (Member m : members) {
            m.setActive(false);
        }
        memberRepository.saveAll(members);
    }



}
