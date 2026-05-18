package com.gr6.SmartCart.modules.fulfillment.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.CancelOrderRequest;
import com.gr6.SmartCart.modules.fulfillment.dto.UpdateShopOrderStatusRequest;

public interface OrderActionService {

    BaseResponse<String> updateShopOrderStatus(
            Long shopOrderId,
            UpdateShopOrderStatusRequest request
    );

    // Luồng cơ bản: Xác nhận chuẩn bị hàng
    BaseResponse<String> confirmOrder(Long orderId);

    // Luồng rẽ nhánh: Hủy đơn và hoàn tồn kho
    BaseResponse<String> cancelOrder(Long orderId, CancelOrderRequest request);
}