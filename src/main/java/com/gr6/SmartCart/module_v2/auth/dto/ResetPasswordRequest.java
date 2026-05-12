// Vị trí: auth/dto/ResetPasswordRequest.java
package com.gr6.SmartCart.module_v2.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Mã OTP không được để trống")
    private String otp;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Mật khẩu phải có ít nhất 1 chữ hoa, 1 số và 1 ký tự đặc biệt"
    )
    private String newPassword;

    @NotBlank(message = "Nhập lại mật khẩu không được để trống")
    private String confirmPassword;
}