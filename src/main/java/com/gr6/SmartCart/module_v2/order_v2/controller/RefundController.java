package com.gr6.SmartCart.module_v2.order_v2.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.module_v2.order_v2.dto.RefundRequest;
import com.gr6.SmartCart.module_v2.order_v2.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/orders/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/{shopOrderId}/cancel")
    public BaseResponse<String> cancelOrder(
            @PathVariable Long shopOrderId, 
            @Valid @RequestBody RefundRequest request) {
        return refundService.cancelOrder(shopOrderId, request);
    }
}