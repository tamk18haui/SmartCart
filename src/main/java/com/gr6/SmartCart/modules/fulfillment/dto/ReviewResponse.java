package com.gr6.SmartCart.modules.fulfillment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {

    private Long reviewId;

    private Long orderId;
    private Long shopOrderId;
    private Long orderItemId;

    private Long productId;
    private String productName;
    private String productImageUrl;

    private Long variantId;
    private String variantSku;

    private Long buyerId;
    private String buyerName;

    private Integer rating;
    private String comment;
    private String imageUrl;

    private String sellerReply;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime repliedAt;
}