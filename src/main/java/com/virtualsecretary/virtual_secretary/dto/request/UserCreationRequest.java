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
    long departmentId;
    @NotNull(message = "Bằng cấp không được để trống")
    Degree degree;
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    String email;
    @Size(min = 3, max = 20, message = "Số căn cước không hợp lệ")
    String identification;
    @Size(max = 255, message = "Địa chỉ quá dài, tối đa 255 ký tự")
    String address;
    @Size(max = 100, message = "Tên ngân hàng quá dài, tối đa 100 ký tự")
    String bankName;
    @Size(min = 5, max = 20, message = "Số tài khoản ngân hàng phải có từ 12 đến 20 chữ số")
    String bankNumber;
    @Size(min = 5, max = 15, message = "Số điện thoại không hợp lệ")
    String phoneNumber;
    @NotNull(message = "Role is required")
    Role role;

}

