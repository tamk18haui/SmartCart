package com.gr6.SmartCart.modules.identity.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.common.enums.UserStatus;
import com.gr6.SmartCart.modules.identity.dto.RegisterRequest;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.identity.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisterServiceImpl implements RegisterService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public BaseResponse<String> register(RegisterRequest request) {
        // 1. Kiểm tra mật khẩu và xác nhận mật khẩu có khớp nhau không
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return BaseResponse.error(400, "Mật khẩu xác nhận không khớp!");
        }

        // 2. Kiểm tra trùng email (Lỗi nghiệp vụ)
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return BaseResponse.error(400, "Lỗi: Email đã tồn tại trong hệ thống!");
        }

        // 3. Nếu mọi thứ ổn, mã hóa mật khẩu và lưu User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        // Mã hóa mật khẩu thô bằng BCrypt trước khi lưu
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setRole(UserRole.BUYER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        return BaseResponse.successMessage("Đăng ký thành công!");
    }
}