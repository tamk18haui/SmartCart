package com.gr6.SmartCart.modules.fulfillment.dto;

import com.gr6.SmartCart.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {

    private Long id;
    private String orderCode;
    private OrderStatus status;
    private LocalDateTime createdAt;

    private BigDecimal totalAmount;
    private BigDecimal subtotalAmount;
    private BigDecimal shippingFee;

    private Long buyerId;
    private String buyerName;

    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;

    private List<OrderItemDTO> items;
}