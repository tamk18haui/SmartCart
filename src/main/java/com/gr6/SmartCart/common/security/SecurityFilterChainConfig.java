package com.gr6.SmartCart.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${frontend-url}")
    private String frontendUrl;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOriginPatterns(java.util.Arrays.stream(frontendUrl.split(","))
                            .map(String::trim)
                            .filter(origin -> !origin.isBlank())
                            .toList());
                    corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    corsConfig.setAllowedHeaders(java.util.List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        /*
                         * =========================
                         * PUBLIC API
                         * =========================
                         */
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",

                                // WebSocket handshake, JWT sẽ được kiểm tra ở StompAuthChannelInterceptor
                                "/ws-chat",
                                "/ws-chat/**",
                                "/ws/**",

                                // Auth v1: login/register hiện tại
                                "/api/v1/auth/**",

                                // Auth v2: forgot-password/reset-password
                                "/api/v2/auth/**",

                                // Buyer đăng ký shop
                                "/api/v1/shops/register",

                                // Storefront public: trang chủ/search/filter sản phẩm
                                "/api/v1/storefront/discovery/**",

                                // Giữ path cũ để không vỡ frontend cũ nếu có
                                "/api/storefront/**",

                                // Chi tiết sản phẩm public
                                "/api/v1/fulfillment/product/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/fulfillment/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/vouchers/shop/**").permitAll()
                        .requestMatchers("/api/v1/payments/**").permitAll()
                        /*
                         * =========================
                         * CATEGORY
                         * =========================
                         * GET danh mục public.
                         * POST/PUT/PATCH danh mục chỉ ADMIN.
                         */
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasAuthority("ADMIN")

                        /*
                         * =========================
                         * RECOMMENDATION (PUBLIC)
                         * =========================
                         * Trending, search, product-based: public.
                         * Personal: cần đăng nhập (anyRequest().authenticated()).
                         */
                        .requestMatchers(HttpMethod.GET, "/api/v3/recommendations/trending").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v3/recommendations/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v3/recommendations/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v3/recommendations/ai/trending").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v3/recommendations/ai/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v3/recommendations/ai/product/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v3/recommendations/ai/image-search").permitAll()
                        /*
                         * =========================
                         * ADMIN
                         * =========================
                         * Quản lý buyer/seller/shop/product/admin moderation.
                         */
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")

                        /*
                         * =========================
                         * SELLER/ADMIN INVENTORY
                         * =========================
                         * Tồn kho là API nhạy cảm, không cho BUYER gọi.
                         * Service vẫn phải check seller sở hữu variant.
                         */
                        .requestMatchers("/api/v1/inventory/**").hasAnyAuthority("SELLER", "ADMIN")

                        /*
                         * =========================
                         * SELLER PRODUCT MANAGEMENT
                         * =========================
                         */
                        .requestMatchers(HttpMethod.POST, "/api/v1/products").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/seller/**").hasAuthority("SELLER")

                        /*
                         * =========================
                         * SELLER VARIANT MANAGEMENT
                         * =========================
                         */
                        .requestMatchers(HttpMethod.POST, "/api/v1/variants").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/variants/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/variants/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/variants/**").hasAuthority("SELLER")

                        /*
                         * =========================
                         * SELLER SHOP MANAGEMENT
                         * =========================
                         */
                        .requestMatchers("/api/v1/shops/update").hasAuthority("SELLER")

                        /*
                         * =========================
                         * SELLER SHOP ORDER MANAGEMENT
                         * =========================
                         * Dùng hasAuthority, không dùng hasRole để tránh lỗi ROLE_SELLER.
                         */
                        .requestMatchers(HttpMethod.GET, "/api/v1/shop-orders/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shop-orders/*/confirm").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shop-orders/*/cancel").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/shop-orders/*/confirm").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/shop-orders/*/cancel").hasAuthority("SELLER")

                        /*
                         * =========================
                         * USER AUTHENTICATED API
                         * =========================
                         * Profile, address, cart, checkout, order buyer...
                         * Không permitAll profile/address/cart.
                         */
                        .requestMatchers("/api/v2/user/profile/**").authenticated()
                        .requestMatchers("/api/v2/customer/addresses/**").authenticated()
                        .requestMatchers("/api/v1/storefront/cart/**").authenticated()
                        .requestMatchers("/api/v1/orders/**").authenticated()
                        .requestMatchers("/api/v1/checkout/**").authenticated()

                        .requestMatchers("/api/v3/admin/withdraw/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v3/seller/withdraw/**").hasAuthority("SELLER")

                        /*
                         * =========================
                         * ANALYTICS (THỐNG KÊ & BÁO CÁO)
                         * =========================
                         */
                        .requestMatchers("/api/v3/admin/analytics/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v3/seller/analytics/**").hasAuthority("SELLER")
                        /*
                         * =========================
                         * DEFAULT
                         * =========================
                         */
                        .requestMatchers("/api/v1/storefront/discovery/**").permitAll()
                        .requestMatchers("/api/v1/chat/**").authenticated()
                        .requestMatchers("/api/v1/payments/**").permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}