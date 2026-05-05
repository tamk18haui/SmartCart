package com.gr6.SmartCart.common.security;

import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// check quyen api  api  nao dc phép  vào đâu
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilterChainConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // Hết đỏ nhờ bước 1

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/api/v1/auth/**","/api/v1/fulfillment/product/**").permitAll()
                        .requestMatchers("/api/v1/shops/update").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/shop-orders/**").hasRole("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shop-orders/*/confirm").hasRole("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shop-orders/*/cancel").hasRole("SELLER")
                        .anyRequest().authenticated()
                );

        // Lắp "máy quét thẻ" vào cửa bảo vệ
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}