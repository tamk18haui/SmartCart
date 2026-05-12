package com.gr6.SmartCart.module_v2.order_v2.dto;

import com.gr6.SmartCart.common.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderTrackingResponse {
    private Long shopOrderId;
    private String shopName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private String receiverName;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private List<TrackingItemDto> items;

    @Data
    @Builder
    public static class TrackingItemDto {
        private String productName;
        private String variantSku;
        private Integer quantity;
        private Long priceAtPurchase;
        private String imageUrl;
    }
}