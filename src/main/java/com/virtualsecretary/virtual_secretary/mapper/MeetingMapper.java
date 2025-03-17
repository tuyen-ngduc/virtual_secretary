package com.virtualsecretary.virtual_secretary.mapper;

import com.virtualsecretary.virtual_secretary.dto.request.MeetingCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.MeetingCreationResponse;
import com.virtualsecretary.virtual_secretary.entity.Meeting;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MeetingMapper {
    Meeting toMeeting(MeetingCreationRequest request);

    MeetingCreationResponse toMeetingResponse(Meeting meeting);
}
