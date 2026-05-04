package com.gr6.SmartCart.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// ham ma hoaa mk
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt sẽ tự động tạo muối (salt) và mã hóa mật khẩu cực kỳ an toàn
        return new BCryptPasswordEncoder();
    }
}