package com.virtualsecretary.virtual_secretary.dto.response;

import com.virtualsecretary.virtual_secretary.enums.Degree;
import com.virtualsecretary.virtual_secretary.enums.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationResponse {
    long id;
    String employeeCode;
    String name;
    LocalDate dob;
    String phoneNumber;
    Degree degree;
    String identification;
    String address;
    String bankName;
    String bankNumber;
    String email;
    String img;
    Role role;
    String departmentName;
}
