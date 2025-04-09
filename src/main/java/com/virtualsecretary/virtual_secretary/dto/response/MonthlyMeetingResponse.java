package com.virtualsecretary.virtual_secretary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyMeetingResponse {
    private Map<String, Map<String, List<MeetingCreationResponse>>> meetingsByWeek;
}

