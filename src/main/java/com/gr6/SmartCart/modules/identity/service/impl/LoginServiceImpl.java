package com.gr6.SmartCart.modules.identity.service.impl;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.security.JwtTokenProvider; // Giả định Tâm đã để ở đây
import com.gr6.SmartCart.modules.identity.dto.LoginRequest;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.identity.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public BaseResponse<Object> login(LoginRequest request) {
        // 1. Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // 2. Kiểm tra user tồn tại và so khớp mật khẩu BCrypt
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return BaseResponse.error(400,"Email hoặc mật khẩu không chính xác");
        }

        // 3. Tạo JWT Token
        String token = jwtTokenProvider.generateToken(user.getEmail());

        // 4. Chuẩn bị dữ liệu trả về
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", token);
        data.put("fullName", user.getFullName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());

        return BaseResponse.success_data("Dang Nhap Thanh cong",data);
    }
}