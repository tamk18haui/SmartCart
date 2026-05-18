// Vị trí: com.gr6.SmartCart.modules.module_v2.auth.service.impl.PasswordResetServiceImpl
package com.gr6.SmartCart.module_v2.auth.service.Impl;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.module_v2.auth.dto.OtpData;
import com.gr6.SmartCart.module_v2.auth.dto.ResetPasswordRequest;
import com.gr6.SmartCart.module_v2.auth.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Map lưu trữ OTP tạm thời trong bộ nhớ (Key: Email)
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    @Override
    public String sendOtp(String email) {
        // Luồng rẽ nhánh 3.2.1: Kiểm tra Email tồn tại
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại trên hệ thống!"));

        // Tạo mã OTP 6 số
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Lưu vào Map kèm thời gian hết hạn 5 phút (Yêu cầu đặc biệt)
        otpStorage.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));

        // Giả lập gửi mail (Log ra console để Hưởng dễ test)
        // tạm thời để thế này tương lai  mới cập  nhật lại thư viện email
        System.out.println(">>> [MAIL SYSTEM] Gửi OTP tới " + email + ": " + otp);
        return "Mã OTP đã được gửi về Email của bạn.";
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // Luồng rẽ nhánh 3.2.3: Kiểm tra mật khẩu mới và nhập lại có khớp không
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu không trùng khớp!");
        }

        OtpData savedOtp = otpStorage.get(request.getEmail());

        // Luồng rẽ nhánh 3.2.2: Kiểm tra OTP hợp lệ và còn hạn
        if (savedOtp == null || !savedOtp.getOtpCode().equals(request.getOtp()) || savedOtp.isExpired()) {
            throw new RuntimeException("Mã OTP không hợp lệ hoặc đã hết thời gian!");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy User!"));

        // Cập nhật mật khẩu đã mã hóa vào bảng Users
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xóa mã OTP khỏi bộ nhớ sau khi hoàn thành
        otpStorage.remove(request.getEmail());
    }
}