package com.gr6.SmartCart.modules.fulfillment.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.CancelOrderRequest;
import com.gr6.SmartCart.modules.fulfillment.dto.UpdateShopOrderStatusRequest;
import com.gr6.SmartCart.modules.fulfillment.service.OrderActionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shop-orders")
@RequiredArgsConstructor
public class OrderActionController {

    private final OrderActionService orderActionService;

    @PutMapping("/{shopOrderId}/status")
    public BaseResponse<String> updateShopOrderStatus(
            @PathVariable Long shopOrderId,
            @Valid @RequestBody UpdateShopOrderStatusRequest request
    ) {
        return orderActionService.updateShopOrderStatus(shopOrderId, request);
    }
    // API Xác nhận chuẩn bị hàng
    @PutMapping("/{id}/confirm")
    public BaseResponse<String> confirm(@PathVariable Long id) {
        return orderActionService.confirmOrder(id);
    }

    // API Hủy đơn hàng
    @PutMapping("/{id}/ ")
    public BaseResponse<String> cancel(@PathVariable Long id, @RequestBody CancelOrderRequest request) {
        return orderActionService.cancelOrder(id, request);
    }
}