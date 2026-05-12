package com.gr6.SmartCart.module_v2.order_v2.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.module_v2.order_v2.dto.RefundRequest;

public interface RefundService {
    BaseResponse<String> cancelOrder(Long shopOrderId, RefundRequest request);
}