package com.gr6.SmartCart.modules.fulfillment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long productId;

    private Long categoryId;
    private String categoryName;

    private String name;
    private String description;
    private String brand;
    private BigDecimal basePrice;
    private List<String> imageUrls;

    private Long shopId;
    private String shopStatus;
    private Long shopOwnerId;
    private String shopName;
    private String shopImageUrl;

    private Integer totalStock;
    private Integer soldQuantity;
    private Integer totalSold;
    private Double averageRating;
    private Integer reviewCount;

    private String status;

    private List<OptionGroupDTO> optionGroups;
    private List<VariantDTO> variants;
    private List<ReviewDTO> reviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionGroupDTO {
        private String name;
        private List<String> values;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDTO {
        private Long variantId;
        private String sku;
        private BigDecimal price;
        private Integer stockQuantity;
        private String imageUrl;
        private Boolean isDefault;
        private String status;
        private Map<String, String> attributes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDTO {
        private Long reviewId;
        private Integer rating;
        private String comment;

        private List<String> imageUrls;
        private String videoUrl;
        private String videoThumbnailUrl;

        private String userName;
        private String sellerReply;
        private LocalDateTime createdAt;
        private LocalDateTime repliedAt;
    }
}