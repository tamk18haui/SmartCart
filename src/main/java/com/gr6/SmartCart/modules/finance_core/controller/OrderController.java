package com.gr6.SmartCart.modules.finance_core.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutPreviewRequest;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutPreviewResponse;
import com.gr6.SmartCart.modules.finance_core.dto.CreateOrderRequest;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCallbackRequest;
import com.gr6.SmartCart.modules.finance_core.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/preview")
    public BaseResponse<CheckoutPreviewResponse> getCheckoutPreview(
            @Valid @RequestBody CheckoutPreviewRequest request
    ) {
        return orderService.getCheckoutPreview(request);
    }

    @PostMapping("/checkout")
    public BaseResponse<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return orderService.createOrder(request);
    }

    /**
     * Callback dev hoặc callback thật sau này.
     * Với production MoMo/VNPay, phải verify signature thật tại đây.
     */
    @PostMapping("/payment/callback")
    public BaseResponse<?> handlePaymentCallback(
            @Valid @RequestBody PaymentCallbackRequest request
    ) {
        return orderService.handlePaymentCallback(request);
    }
}