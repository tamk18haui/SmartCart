// Vị trí: auth/dto/OtpData.java
package com.gr6.SmartCart.module_v2.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OtpData {
    private String otpCode;
    private LocalDateTime expiryTime;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}