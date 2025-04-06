package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.request.AddMemberRequest;
import com.virtualsecretary.virtual_secretary.dto.request.RemoveMemberRequest;
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

    @DeleteMapping("/remove")
    public ApiResponse<Void> removeMember(@RequestBody @Valid RemoveMemberRequest request) {
        memberService.removeMemberFromMeeting(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa thành viên khỏi cuộc họp thành công")
                .build();

    }
    @PutMapping("/update")
    public ApiResponse<MemberResponse> updateMeetingRole(@RequestBody @Valid AddMemberRequest request) {
        MemberResponse updatedMember = memberService.updateMeetingRole(request);
        return ApiResponse.<MemberResponse>builder()
                .code(200)
                .message("Cập nhật meetingRole thành công")
                .result(updatedMember)
                .build();
    }
}