package com.gr6.SmartCart.module_v2.order_v2.dto;

import com.gr6.SmartCart.common.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderHistoryResponse {
    private Long shopOrderId;
    private String shopName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}