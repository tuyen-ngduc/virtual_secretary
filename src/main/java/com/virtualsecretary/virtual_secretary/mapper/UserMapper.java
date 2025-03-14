package com.virtualsecretary.virtual_secretary.mapper;

import com.virtualsecretary.virtual_secretary.dto.request.UserCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.request.UserUpdateRequest;
import com.virtualsecretary.virtual_secretary.dto.response.UserResponse;
import com.virtualsecretary.virtual_secretary.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
