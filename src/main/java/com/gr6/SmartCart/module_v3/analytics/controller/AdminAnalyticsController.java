package com.gr6.SmartCart.module_v3.analytics.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.module_v3.analytics.dto.request.DateFilterRequest;
import com.gr6.SmartCart.module_v3.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v3/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/top-products")
    public ResponseEntity<?> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            @Valid @RequestBody DateFilterRequest filter) {
        // shopId = null để lấy toàn hệ thống
        return ResponseEntity.ok(BaseResponse.success_data(
                "Lấy danh sách sản phẩm bán chạy toàn hệ thống thành công",
                analyticsService.getTopSellingProducts(null, filter, limit)
        ));
    }

    @PostMapping("/revenue")
    public ResponseEntity<?> getSystemRevenue(@Valid @RequestBody DateFilterRequest filter) {
        return ResponseEntity.ok(BaseResponse.success_data(
                "Lấy báo cáo doanh thu toàn hệ thống thành công",
                analyticsService.getRevenueReport(null, filter)
        ));
    }

    @PostMapping("/transactions")
    public ResponseEntity<?> getTransactionStats(@Valid @RequestBody DateFilterRequest filter) {
        return ResponseEntity.ok(BaseResponse.success_data(
                "Lấy thống kê giao dịch thành công",
                analyticsService.getTransactionStats(filter)
        ));
    }
}