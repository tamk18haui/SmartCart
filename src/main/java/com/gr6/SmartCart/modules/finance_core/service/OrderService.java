package com.gr6.SmartCart.modules.finance_core.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Order;
import com.gr6.SmartCart.modules.finance_core.dto.CreateOrderRequest;

public interface OrderService {
    BaseResponse<?> createOrder(CreateOrderRequest request);
}
