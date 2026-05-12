package com.gr6.SmartCart.modules.fulfillment.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.UpdateShopOrderStatusRequest;

public interface OrderActionService {

    BaseResponse<String> updateShopOrderStatus(
            Long shopOrderId,
            UpdateShopOrderStatusRequest request
    );
}