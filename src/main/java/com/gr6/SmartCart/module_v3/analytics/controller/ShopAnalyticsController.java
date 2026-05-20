package com.gr6.SmartCart.module_v3.analytics.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.exception.CustomException;
import com.gr6.SmartCart.module_v3.analytics.dto.request.DateFilterRequest;
import com.gr6.SmartCart.module_v3.analytics.service.AnalyticsService;
import com.gr6.SmartCart.modules.identity.repository.ShopRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;

@RestController
@RequestMapping("/api/v3/seller/analytics")
@RequiredArgsConstructor
public class ShopAnalyticsController {

    private final AnalyticsService analyticsService;
    private final ShopRepository shopRepository;

    // Hàm dùng chung để lấy shopId từ Principal, code nhìn sẽ rất clear và không bị lặp
    private Long getShopIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new CustomException(401, "Bạn chưa đăng nhập!");
        }

        String email = principal.getName();
        Long shopId = shopRepository.findShopIdByEmail(email);

        if (shopId == null) {
            throw new CustomException(403, "Tài khoản của bạn chưa đăng ký cửa hàng!");
        }
        return shopId;
    }

    @PostMapping("/top-products")
    public ResponseEntity<?> getTopProducts(
            Principal principal,
            @RequestParam(defaultValue = "10") int limit,
            @Valid @RequestBody DateFilterRequest filter) {

        // Tự động bóc tách email từ token và truy xuất shopId
        Long shopId = getShopIdFromPrincipal(principal);

        return ResponseEntity.ok(BaseResponse.success_data(
                "Lấy danh sách sản phẩm bán chạy thành công",
                analyticsService.getTopSellingProducts(shopId, filter, limit)
        ));
    }

    @PostMapping("/revenue")
    public ResponseEntity<?> getShopRevenue(
            Principal principal,
            @Valid @RequestBody DateFilterRequest filter) {

        // Tự động bóc tách email từ token và truy xuất shopId
        Long shopId = getShopIdFromPrincipal(principal);

        return ResponseEntity.ok(BaseResponse.success_data(
                "Lấy báo cáo doanh thu cửa hàng thành công",
                analyticsService.getRevenueReport(shopId, filter)
        ));
    }
}