package com.gr6.SmartCart.modules.fulfillment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
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
    public static class OptionGroupDTO {
        private String name;
        private List<String> values;
    }

    @Data
    @Builder
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
    public static class ReviewDTO {
        private Integer rating;
        private String comment;
        private String imageUrl;
        private String userName;
    }
}