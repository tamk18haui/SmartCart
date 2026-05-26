package com.gr6.SmartCart.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenRequest {

    @NotBlank(message = "fcmToken không được trống")
    private String fcmToken;

    private String deviceId;

    private String platform = "ANDROID";
}
