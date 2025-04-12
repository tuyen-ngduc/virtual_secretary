package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.payload.Signal;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class MeetingParticipantManager {
    private final Map<String, List<Signal>> participants = new ConcurrentHashMap<>();

    private final Map<String, String> sessionPeerMap = new ConcurrentHashMap<>();

    private final Map<String, String> peerRoomMap = new ConcurrentHashMap<>();

    public void addParticipant(String meetingCode, Signal member) {
        participants.computeIfAbsent(meetingCode, k -> new ArrayList<>()).add(member);
    }

    public void removeParticipant(String meetingCode, String peerId) {
        participants.computeIfPresent(meetingCode, (k, list) -> {
            list.removeIf(m -> m.getFrom().equals(peerId));
            return list.isEmpty() ? null : list;
        });
    }

    public List<Signal> getParticipants(String meetingCode, String excludePeerId) {
        return participants.getOrDefault(meetingCode, new ArrayList<>())
                .stream()
                .filter(m -> !m.getFrom().equals(excludePeerId))
                .collect(Collectors.toList());
    }

}
