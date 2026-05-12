package com.gr6.SmartCart.modules.fulfillment.dto;

import com.gr6.SmartCart.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateShopOrderStatusRequest {

    @NotNull(message = "Vui lòng chọn trạng thái đơn hàng")
    private OrderStatus status;

    private String cancelReason;
}