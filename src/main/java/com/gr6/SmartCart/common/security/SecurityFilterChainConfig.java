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
                        // API public
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/auth/**",
                                "/api/v1/shops/register",

                                // Storefront public đúng version API hiện tại.
                                // DiscoveryController đang chạy ở /api/v1/storefront/discovery.
                                "/api/v1/storefront/discovery/**",

                                // Giữ lại path cũ để không làm vỡ frontend nếu trước đó có gọi.
                                "/api/storefront/**",

                                // Chi tiết sản phẩm public.
                                "/api/v1/fulfillment/product/**"
                        ).permitAll()

                        // Danh mục: public GET, còn tạo/sửa/ẩn hiện là ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/categories/**").hasAuthority("ADMIN")

                        // Admin
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")

                        // Seller/Admin inventory
                        .requestMatchers("/api/v1/inventory/**").hasAnyAuthority("SELLER", "ADMIN")

                        // Seller product management
                        .requestMatchers(HttpMethod.POST, "/api/v1/products").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/seller/**").hasAuthority("SELLER")

                        // Seller variant management
                        .requestMatchers(HttpMethod.POST, "/api/v1/variants").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/variants/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/variants/**").hasAuthority("SELLER")

                        // Seller shop/order management
                        .requestMatchers("/api/v1/shops/update").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/shop-orders/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shop-orders/*/confirm").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shop-orders/*/cancel").hasAuthority("SELLER")

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}