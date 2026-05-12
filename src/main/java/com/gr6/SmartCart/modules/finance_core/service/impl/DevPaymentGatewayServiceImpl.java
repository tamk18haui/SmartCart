package com.gr6.SmartCart.modules.finance_core.service.impl;

import com.gr6.SmartCart.common.domain.Order;
import com.gr6.SmartCart.common.domain.Transaction;
import com.gr6.SmartCart.common.enums.PaymentProvider;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCreateResult;
import com.gr6.SmartCart.modules.finance_core.service.PaymentGatewayService;
import org.springframework.stereotype.Service;

@Service
public class DevPaymentGatewayServiceImpl implements PaymentGatewayService {

    public static final String DEV_PAYMENT_SIGNATURE = "SMARTCART_DEV_PAYMENT_SECRET";

    @Override
    public PaymentCreateResult createPaymentUrl(
            Order order,
            Transaction transaction,
            PaymentProvider provider
    ) {
        if (provider == null || provider == PaymentProvider.NONE) {
            throw new RuntimeException("Vui lòng chọn cổng thanh toán MOMO hoặc VNPAY");
        }

        String providerTransactionId = "DEV-" + provider.name()
                + "-ORDER-" + order.getOrderId()
                + "-TX-" + transaction.getTransactionId()
                + "-" + System.currentTimeMillis();

        /**
         * Link dev:
         * - Mở link success để giả lập thanh toán thành công.
         * - Đổi /success thành /fail để giả lập thất bại.
         *
         * Nếu chạy local port khác, sửa baseUrl.
         */
        String baseUrl = "http://localhost:8080";

        String paymentUrl = baseUrl
                + "/api/v1/payments/dev/" + provider.name().toLowerCase()
                + "/success"
                + "?orderId=" + order.getOrderId()
                + "&transactionId=" + transaction.getTransactionId()
                + "&providerTransactionId=" + providerTransactionId
                + "&signature=" + DEV_PAYMENT_SIGNATURE;

        return PaymentCreateResult.builder()
                .paymentUrl(paymentUrl)
                .providerTransactionId(providerTransactionId)
                .build();
    }
}