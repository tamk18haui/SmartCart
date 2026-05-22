package com.gr6.SmartCart.modules.fulfillment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private String buyerAvatarUrl;

    private Integer rating;
    private String comment;

    // Tối đa 4 ảnh review
    private List<String> imageUrls;

    // 1 video review
    private String videoUrl;

    private String sellerReply;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime repliedAt;
}