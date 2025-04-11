package com.virtualsecretary.virtual_secretary.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProfileRequest {
    @NotBlank(message = "Tên không được để trống")
    String name;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải nhỏ hơn hiện tại")
    LocalDate dob;

    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không hợp lệ")
    String phoneNumber;

    @Size(min = 9, max = 12, message = "CMND/CCCD phải từ 9 đến 12 ký tự")
    String identification;

    String address;

    String bankName;

    @Pattern(regexp = "\\d{9,20}", message = "Số tài khoản ngân hàng không hợp lệ")
    String bankNumber;

    @Email(message = "Email không hợp lệ")
    String email;

    String img;
}

