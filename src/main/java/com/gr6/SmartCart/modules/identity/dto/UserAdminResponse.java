package com.gr6.SmartCart.modules.identity.dto;

import com.gr6.SmartCart.common.domain.User;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserAdminResponse {
    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    public static UserAdminResponse fromEntity(User user) {
        return UserAdminResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}