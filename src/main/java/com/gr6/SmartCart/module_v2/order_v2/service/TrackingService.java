package com.gr6.SmartCart.module_v2.order_v2.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.module_v2.order_v2.dto.OrderHistoryResponse;
import com.gr6.SmartCart.module_v2.order_v2.dto.OrderTrackingResponse;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutOrderResponse;

import java.util.List;

public interface TrackingService {

    BaseResponse<List<OrderHistoryResponse>> getOrderHistory();

    BaseResponse<OrderTrackingResponse> trackOrder(Long shopOrderId);

    BaseResponse<String> completeBuyerOrder(Long shopOrderId);

    BaseResponse<CheckoutOrderResponse> retryPayment(Long shopOrderId);
}