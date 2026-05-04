package com.gr6.SmartCart.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityFilterChainConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF để có thể test API qua Postman/Swagger mà không cần token bảo mật trình duyệt [cite: 557]
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Mở cửa cho Swagger UI và tài liệu API để bạn xem "docs" [cite: 569, 570]
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // 2. Mở cửa cho các API Đăng ký và Đăng nhập (SMAR-5, SMAR-11) [cite: 71, 557]
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/shops/**").permitAll()
                        // 3. Các yêu cầu còn lại đều phải đăng nhập mới được truy cập
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}