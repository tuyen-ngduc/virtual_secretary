package com.virtualsecretary.virtual_secretary.mapper;

import com.virtualsecretary.virtual_secretary.dto.response.MemberResponse;
import com.virtualsecretary.virtual_secretary.entity.Member;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toMemberResponse(Member member);

}
