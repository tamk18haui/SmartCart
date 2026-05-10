// Vị trí: com.gr6.SmartCart.modules.module_v2.auth.service.PasswordResetService
package com.gr6.SmartCart.module_v2.auth.service;

import com.gr6.SmartCart.module_v2.auth.dto.ResetPasswordRequest;

public interface PasswordResetService {
    // 3.1. Luồng cơ bản - Bước 2: Kiểm tra tài khoản và gửi OTP
    String sendOtp(String email);

    // 3.1. Luồng cơ bản - Bước 3: Xác nhận OTP và lưu mật khẩu mới
    void resetPassword(ResetPasswordRequest request);
}