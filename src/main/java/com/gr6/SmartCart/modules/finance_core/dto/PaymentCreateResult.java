package com.gr6.SmartCart.modules.finance_core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCreateResult {
    private String paymentUrl;
    private String providerTransactionId;
}