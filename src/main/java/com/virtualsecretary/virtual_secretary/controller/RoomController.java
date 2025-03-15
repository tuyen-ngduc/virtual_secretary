package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.entity.Room;
import com.virtualsecretary.virtual_secretary.service.RoomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomController {
    RoomService roomService;

    @GetMapping
    public ApiResponse<List<Room>> getAllRooms() {
        return ApiResponse.<List<Room>>builder()
                .code(200)
                .result(roomService.getAllRooms())
                .build();
    }

    @GetMapping("/{roomId}")
    public ApiResponse<Room> getRoomById(@PathVariable long roomId) {
        return ApiResponse.<Room>builder()
                .code(200)
                .result(roomService.getRoomById(roomId))
                .build();
    }

    @PostMapping
    public ApiResponse<Room> createRoom(@RequestBody Room room) {
        return ApiResponse.<Room>builder()
                .code(201)
                .result(roomService.createRoom(room))
                .build();
    }


    @PutMapping("/{id}")
    public ApiResponse<Room> updateRoom(@PathVariable long id, @RequestBody Room room) {
        return ApiResponse.<Room>builder()
                .code(200)
                .result(roomService.updateRoom(id, room))
                .build();
    }


    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRoom(@PathVariable long id) {
        roomService.deleteRoom(id);
        return ApiResponse.<Void>builder()
                        .code(200)
                        .build();
    }
}
