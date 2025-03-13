package com.virtualsecretary.virtual_secretary.dto.request;

import com.virtualsecretary.virtual_secretary.enums.Degree;
import com.virtualsecretary.virtual_secretary.enums.Role;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "Tên không được để trống")
    @Size(min = 4, max = 40, message = "Độ dài tên không phù hợp")
    String name;
    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(min = 3, max = 9, message = "Tên phải có độ dài từ 3 đến 9 ký tự")
    String employeeCode;
    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh không hợp lệ")
    LocalDate dob;
    @NotNull(message = "Phòng ban không được để trống")
    long departmentId;
    @NotNull(message = "Bằng cấp không được để trống")
    Degree degree;
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    String email;

}

