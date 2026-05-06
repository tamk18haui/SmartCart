package com.gr6.SmartCart.modules.fulfillment.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.OrderDetailResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.OrderListResponse;
import java.util.List;

public interface OrderQueryService {
    // Lấy danh sách + Tìm kiếm (Luồng cơ bản 1 & Rẽ nhánh 2)
    BaseResponse<List<OrderListResponse>> getAllOrders(String keyword);

    // Xem chi tiết (Luồng cơ bản 2 & 3)
    BaseResponse<OrderDetailResponse> getOrderDetail(Long orderId);
}