package com.gr6.SmartCart.modules.finance_core.dto;

import com.gr6.SmartCart.common.enums.PaymentProvider;
import lombok.Data;

@Data
public class PaymentCallbackRequest {

    private Long orderId;

    private Long transactionId;

    private PaymentProvider paymentProvider;

    private String providerTransactionId;

    private Boolean success;

    private String signature;
}