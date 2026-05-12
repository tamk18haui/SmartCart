// Vị trí: auth/controller/PasswordController.java
package com.gr6.SmartCart.module_v2.auth.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.module_v2.auth.dto.ResetPasswordRequest;
import com.gr6.SmartCart.module_v2.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public BaseResponse<String> forgotPassword(@RequestParam String email) {
        String msg = passwordResetService.sendOtp(email);
        return BaseResponse.success(msg);
    }

    @PostMapping("/reset-password")
    public BaseResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return BaseResponse.success("Đổi mật khẩu thành công!");
    }
}