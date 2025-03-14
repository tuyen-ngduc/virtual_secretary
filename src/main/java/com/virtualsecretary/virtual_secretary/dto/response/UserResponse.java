package com.virtualsecretary.virtual_secretary.dto.response;

import com.virtualsecretary.virtual_secretary.entity.Department;
import com.virtualsecretary.virtual_secretary.enums.Degree;
import com.virtualsecretary.virtual_secretary.enums.Role;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    long id;
    String employeeCode;
    Department department;
    String name;
    LocalDate dob;
    String phoneNumber;
    Degree degree;
    String identification;
    String address;
    String bankName;
    String bankNumber;
    String email;
    Role role;
}
