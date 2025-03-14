package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.request.UserCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.request.UserUpdateRequest;
import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.dto.response.UserResponse;
import com.virtualsecretary.virtual_secretary.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/create")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Employee account created successfully")
                .result(userService.createUser(request))
                .build();
    }
    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .code(200)
                .result(userService.getAllUsers())
                .build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable long userId, @RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Employee account updated successfully")
                .result(userService.updateUser(userId, request))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .code(204)
                .message("User has been deleted")
                .build();
    }



}
