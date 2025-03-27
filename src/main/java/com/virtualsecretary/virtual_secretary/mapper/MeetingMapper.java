package com.virtualsecretary.virtual_secretary.mapper;

import com.virtualsecretary.virtual_secretary.dto.request.MeetingCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.MeetingCreationResponse;
import com.virtualsecretary.virtual_secretary.entity.Meeting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MeetingMapper {

    Meeting toMeeting(MeetingCreationRequest request);

    @Mapping(target = "status", expression = "java(meeting.getMeetingStatus())")
    MeetingCreationResponse toMeetingCreationResponse(Meeting meeting);
}
