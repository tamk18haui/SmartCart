package com.gr6.SmartCart.modules.finance_core.dto;

import com.gr6.SmartCart.common.enums.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentCallbackRequest {

    @NotNull(message = "Thiếu orderId")
    private Long orderId;

    private Long transactionId;

    private String providerTransactionId;

    private PaymentProvider paymentProvider;

    @NotNull(message = "Thiếu trạng thái thanh toán")
    private Boolean success;

    /**
     * Dev secret để tránh ai cũng gọi success bừa.
     * Production phải verify chữ ký MoMo/VNPay thật.
     */
    private String signature;
}