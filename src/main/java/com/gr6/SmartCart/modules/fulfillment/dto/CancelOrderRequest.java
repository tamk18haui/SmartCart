package com.gr6.SmartCart.modules.fulfillment.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CancelOrderRequest {
    @NotBlank(message = "Vui lòng cung cấp lý do hủy đơn!")
    private String cancelReason;
}
