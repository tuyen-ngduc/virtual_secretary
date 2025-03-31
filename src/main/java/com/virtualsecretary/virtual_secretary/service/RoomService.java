package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.entity.Room;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.repository.MeetingRepository;
import com.virtualsecretary.virtual_secretary.repository.RoomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class RoomService {
    MeetingRepository meetingRepository;
    RoomRepository roomRepository;
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public Room getRoomById(long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IndicateException((ErrorCode.ROOM_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Room createRoom(Room room) {
        if (roomRepository.existsByRoomCode(room.getRoomCode())) {
            throw new IndicateException(ErrorCode.ROOM_CODE_ALREADY_EXISTS);
        }
        return roomRepository.save(room);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public Room updateRoom(long id, Room updatedRoom) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new IndicateException(ErrorCode.ROOM_NOT_EXISTED));

        if (!existingRoom.getRoomCode().equals(updatedRoom.getRoomCode()) &&
                roomRepository.existsByRoomCode(updatedRoom.getRoomCode())) {
            throw new IndicateException(ErrorCode.ROOM_CODE_ALREADY_EXISTS);
        }

        existingRoom.setRoomCode(updatedRoom.getRoomCode());
        existingRoom.setRoomName(updatedRoom.getRoomName());

        return roomRepository.save(existingRoom);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRoom(long id) {
        if (!roomRepository.existsById(id)) {
            throw new IndicateException(ErrorCode.ROOM_NOT_EXISTED);
        }

        meetingRepository.updateRoomInMeetingToNull(id);
        roomRepository.deleteById(id);
    }

    

}