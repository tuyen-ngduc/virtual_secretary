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
        String formattedDob = request.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String password = passwordEncoder.encode(formattedDob);
        User user = userMapper.toUser(request);
        user.setDepartment(department);
        user.setPassword(password);
        user.setRole(Role.USER);
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