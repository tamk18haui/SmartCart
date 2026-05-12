package com.gr6.SmartCart.modules.finance_core.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.enums.PaymentProvider;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCallbackRequest;
import com.gr6.SmartCart.modules.finance_core.service.OrderService;
import com.gr6.SmartCart.modules.finance_core.service.impl.DevPaymentGatewayServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/dev")
@RequiredArgsConstructor
public class DevPaymentController {

    private final OrderService orderService;

    @GetMapping("/{provider}/success")
    public BaseResponse<?> paymentSuccess(
            @PathVariable String provider,
            @RequestParam Long orderId,
            @RequestParam Long transactionId,
            @RequestParam String providerTransactionId,
            @RequestParam String signature
    ) {
        PaymentCallbackRequest request = new PaymentCallbackRequest();
        request.setOrderId(orderId);
        request.setTransactionId(transactionId);
        request.setProviderTransactionId(providerTransactionId);
        request.setPaymentProvider(resolveProvider(provider));
        request.setSuccess(true);
        request.setSignature(signature);

        return orderService.handlePaymentCallback(request);
    }

    @GetMapping("/{provider}/fail")
    public BaseResponse<?> paymentFail(
            @PathVariable String provider,
            @RequestParam Long orderId,
            @RequestParam Long transactionId,
            @RequestParam String providerTransactionId,
            @RequestParam String signature
    ) {
        PaymentCallbackRequest request = new PaymentCallbackRequest();
        request.setOrderId(orderId);
        request.setTransactionId(transactionId);
        request.setProviderTransactionId(providerTransactionId);
        request.setPaymentProvider(resolveProvider(provider));
        request.setSuccess(false);
        request.setSignature(signature);

        return orderService.handlePaymentCallback(request);
    }

    private PaymentProvider resolveProvider(String provider) {
        if ("momo".equalsIgnoreCase(provider)) {
            return PaymentProvider.MOMO;
        }

        if ("vnpay".equalsIgnoreCase(provider)) {
            return PaymentProvider.VNPAY;
        }

        throw new RuntimeException("Cổng thanh toán không hợp lệ");
    }
}