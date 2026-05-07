package com.gr6.SmartCart.modules.fulfillment.dto;

import com.gr6.SmartCart.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {
    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;

    // Thông tin người nhận (Từ bảng Addresses)
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;

    // Danh sách sản phẩm (Từ bảng Order_Items)
    private List<OrderItemDTO> items;
}