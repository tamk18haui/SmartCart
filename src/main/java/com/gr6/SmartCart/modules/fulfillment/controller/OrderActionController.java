package com.gr6.SmartCart.modules.fulfillment.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
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
}