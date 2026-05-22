package com.gr6.SmartCart.modules.fulfillment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewableOrderItemResponse {

    private Long orderId;
    private Long shopOrderId;
    private Long orderItemId;

    private Long productId;
    private String productName;
    private String productImageUrl;

    private Long variantId;
    private String variantSku;

    private Integer quantity;
    private Long priceAtPurchase;

    private Long shopId;
    private String shopName;
}