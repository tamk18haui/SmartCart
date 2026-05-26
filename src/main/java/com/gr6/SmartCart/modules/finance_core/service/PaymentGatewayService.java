package com.gr6.SmartCart.modules.finance_core.service;

import com.gr6.SmartCart.common.domain.Order;
import com.gr6.SmartCart.common.domain.Transaction;
import com.gr6.SmartCart.common.enums.PaymentProvider;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCreateResult;

public interface PaymentGatewayService {

    PaymentCreateResult createPaymentUrl(
            Order order,
            Transaction transaction,
            PaymentProvider provider
    );
}