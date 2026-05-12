package com.gr6.SmartCart.module_v2.order_v2.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.module_v2.order_v2.dto.OrderHistoryResponse;
import com.gr6.SmartCart.module_v2.order_v2.dto.OrderTrackingResponse;
import com.gr6.SmartCart.module_v2.order_v2.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/orders/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping("/history")
    public BaseResponse<List<OrderHistoryResponse>> getOrderHistory() {
        return trackingService.getOrderHistory();
    }

    @GetMapping("/{shopOrderId}")
    public BaseResponse<OrderTrackingResponse> trackOrder(@PathVariable Long shopOrderId) {
        return trackingService.trackOrder(shopOrderId);
    }
}