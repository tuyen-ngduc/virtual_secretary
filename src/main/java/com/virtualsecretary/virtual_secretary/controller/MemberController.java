package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.request.AddMemberRequest;
import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.dto.response.MemberResponse;
import com.virtualsecretary.virtual_secretary.service.MemberService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberController {
    MemberService memberService;

    @PostMapping("/add")
    public ApiResponse<MemberResponse> addMember(@RequestBody @Valid AddMemberRequest request) {
        return ApiResponse.<MemberResponse>builder()
                .code(200)
                .message("Add member successfully")
                .result(memberService.addMemberToMeeting(request))
                .build();
    }
    @GetMapping("/{meetingId}")
    public ApiResponse<List<MemberResponse>> getAllMembers(@PathVariable Long meetingId) {
        return ApiResponse.<List<MemberResponse>>builder()
                .code(200)
                .result(memberService.getMembersByMeetingId(meetingId)).build();
    }



}
