package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.dto.request.UserCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.request.UserUpdateRequest;
import com.virtualsecretary.virtual_secretary.dto.response.UserResponse;
import com.virtualsecretary.virtual_secretary.entity.Department;
import com.virtualsecretary.virtual_secretary.entity.User;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.enums.Role;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.mapper.UserMapper;
import com.virtualsecretary.virtual_secretary.repository.DepartmentRepository;
import com.virtualsecretary.virtual_secretary.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@Slf4j
public class UserService {
    UserRepository userRepository;
    DepartmentRepository departmentRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(UserCreationRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IndicateException(ErrorCode.DEPARTMENT_NOT_EXISTED));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IndicateException(ErrorCode.EMAIL_EXISTED);
        }
        if (userRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new IndicateException(ErrorCode.USER_EXISTED);
        }
        if (request.getRole() == null || (!request.getRole().equals(Role.ROLE_USER) && !request.getRole().equals(Role.ROLE_SECRETARY))) {
            throw new IndicateException(ErrorCode.ROLE_INVALID);
        }
        String formattedDob = request.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String password = passwordEncoder.encode(formattedDob);
        User user = userMapper.toUser(request);
        user.setDepartment(department);
        user.setPassword(password);
        user.setImg("data:image/jpeg;base64,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");
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IndicateException(ErrorCode.USER_NOT_EXISTED));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IndicateException(ErrorCode.DEPARTMENT_NOT_EXISTED));

        if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new IndicateException(ErrorCode.EMAIL_EXISTED);
        }
        if (userRepository.existsByEmployeeCodeAndIdNot(request.getEmployeeCode(), userId)) {
            throw new IndicateException(ErrorCode.USER_EXISTED);
        }

        if (request.getRole() == null || (!request.getRole().equals(Role.ROLE_USER) && !request.getRole().equals(Role.ROLE_SECRETARY))) {
            throw new IndicateException(ErrorCode.ROLE_INVALID);
        }

        String formattedDob = request.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String password = passwordEncoder.encode(formattedDob);
        userMapper.updateUser(user, request);
        user.setDepartment(department);
        user.setPassword(password);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(long userId) {
        return userMapper.toUserResponse(
                userRepository.findById(userId).orElseThrow(() -> new IndicateException(ErrorCode.USER_NOT_EXISTED)));
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByEmployeeCode(name).orElseThrow(() -> new IndicateException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }
}