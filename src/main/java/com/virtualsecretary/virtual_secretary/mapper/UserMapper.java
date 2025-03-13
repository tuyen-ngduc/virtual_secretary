package com.virtualsecretary.virtual_secretary.mapper;

import com.virtualsecretary.virtual_secretary.dto.request.UserCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.UserCreationResponse;
import com.virtualsecretary.virtual_secretary.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserCreationResponse toUserCreationResponse(User user);
}
