package com.gr6.SmartCart.module_v2.order_v2.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundRequest {
    @NotBlank(message = "Vui lòng nhập lý do hủy đơn")
    private String reason;
}