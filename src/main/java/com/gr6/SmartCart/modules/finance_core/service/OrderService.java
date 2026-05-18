package com.gr6.SmartCart.modules.finance_core.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutPreviewRequest;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutPreviewResponse;
import com.gr6.SmartCart.modules.finance_core.dto.CreateOrderRequest;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCallbackRequest;

public interface OrderService {

    BaseResponse<CheckoutPreviewResponse> getCheckoutPreview(CheckoutPreviewRequest request);

    BaseResponse<?> createOrder(CreateOrderRequest request);

    BaseResponse<?> handlePaymentCallback(PaymentCallbackRequest request);
}