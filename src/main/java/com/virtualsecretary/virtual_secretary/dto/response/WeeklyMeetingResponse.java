package com.virtualsecretary.virtual_secretary.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeeklyMeetingResponse {
    Map<String, List<MeetingCreationResponse>> meetingsByDay;

}
