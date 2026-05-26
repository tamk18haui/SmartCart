package com.gr6.SmartCart.modules.notification.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.notification.dto.FcmTokenRequest;
import com.gr6.SmartCart.modules.notification.service.FcmTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/token")
    public BaseResponse<?> saveToken(
            Authentication authentication,
            @Valid @RequestBody FcmTokenRequest request
    ) {
        fcmTokenService.saveToken(authentication.getName(), request);
        return BaseResponse.successMessage("Lưu FCM token thành công");
    }

    @DeleteMapping("/token")
    public BaseResponse<?> disableToken(
            @RequestParam String fcmToken
    ) {
        fcmTokenService.disableToken(fcmToken);
        return BaseResponse.successMessage("Đã hủy FCM token");
    }
}