package com.gr6.SmartCart.module_v2.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileDTO {
    private String fullName;
    private String email;
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^0\\d{9}$",
            message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0"
    )
    private String phoneNumber;
    private String avatarUrl;
    private String role; // Để Frontend biết quyền của User (BUYER/SELLER)
}