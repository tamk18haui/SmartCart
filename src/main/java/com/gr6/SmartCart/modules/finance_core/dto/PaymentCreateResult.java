package com.gr6.SmartCart.modules.finance_core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateResult {

    private String paymentUrl;

    /*
     * Với VNPay, mình dùng providerTransactionId = orderId-transactionId.
     * Khi VNPay callback về vnp_TxnRef, backend sẽ tách lại được orderId và transactionId.
     */
    private String providerTransactionId;
}