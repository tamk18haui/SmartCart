package com.gr6.SmartCart.modules.fulfillment.dto;

import com.gr6.SmartCart.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderListResponse {
    private Long id;
    private String orderCode; // Mã đơn hàng để hiển thị
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
}