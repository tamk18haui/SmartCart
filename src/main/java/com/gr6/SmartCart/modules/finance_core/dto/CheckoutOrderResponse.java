package com.gr6.SmartCart.modules.finance_core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutOrderResponse {
    private Long orderId;
    private Long shopOrderId;
    private Long transactionId;
    private String paymentUrl;
    private String orderStatus;
    private String paymentStatus;
    private String paymentProvider;
    private String checkoutSource;
    private Long totalAmount;
}