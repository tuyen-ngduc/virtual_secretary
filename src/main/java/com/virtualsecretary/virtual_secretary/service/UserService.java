package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.dto.request.UserCreationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.UserCreationResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class UserService {
    UserRepository userRepository;
    DepartmentRepository departmentRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    public UserCreationResponse createUser(UserCreationRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IndicateException(ErrorCode.DEPARTMENT_NOT_EXISTED));

        if(userRepository.existsByEmail(request.getEmail())){
            throw new IndicateException(ErrorCode.EMAIL_EXISTED);
        }
        if(userRepository.existsByEmployeeCode(request.getEmployeeCode())){
            throw new IndicateException(ErrorCode.USER_EXISTED);
        }
        String formattedDob = request.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String password = passwordEncoder.encode(formattedDob);
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(password)
                .role(Role.USER)
                .department(department)
                .employeeCode(request.getEmployeeCode())
                .dob(request.getDob())
                .degree(request.getDegree())
                .build();
        userRepository.save(user);
        return userMapper.toUserCreationResponse(user);
    }
}
