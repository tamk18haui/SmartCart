package com.gr6.SmartCart.module_v2.order_v2.dto;

import com.gr6.SmartCart.common.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderHistoryResponse {

    // ID đơn tổng trong bảng Orders
    private Long orderId;

    // ID đơn theo shop trong bảng Shop_Orders
    private Long shopOrderId;

    private Long shopId;
    private String shopName;

    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    // Danh sách sản phẩm thuộc đúng orderId + shopId này
    private List<OrderHistoryItemResponse> items;

    @Data
    @Builder
    public static class OrderHistoryItemResponse {
        private Long orderItemId;

        private Long productId;
        private Long variantId;

        private String productName;
        private String variantSku;

        private Integer quantity;
        private Long priceAtPurchase;

        private String imageUrl;
        private Boolean canReview;
        private Boolean reviewed;
    }
}