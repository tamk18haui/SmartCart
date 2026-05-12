package com.gr6.SmartCart.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilterChainConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Gộp chung các endpoint công khai: Swagger, Auth, và Storefront (từ Sáng)
                        // Bao gồm cả endpoint fulfillment/product từ phiên bản local của bạn
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/auth/**",
                                "/api/v1/shops/register",
                                "/api/storefront/**",
                                "/api/v1/fulfillment/product/**",
                                "/api/v2/auth/**",
                                "/api/v2/user/profile",
                                "/api/v2/seller/vouchers/**"
                        ).permitAll()

                        // 2. API xem danh mục dành cho khách vãng lai (từ Sáng)
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()

                        // 3. Các quyền hạn dành riêng cho SELLER (Người bán)
                        .requestMatchers("/api/v1/shops/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/shop-orders/**").hasRole("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shop-orders/*/confirm").hasRole("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shop-orders/*/cancel").hasRole("SELLER")

                        // 4. Các yêu cầu còn lại đều phải xác thực
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}