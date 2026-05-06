package com.gr6.SmartCart.modules.fulfillment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponse {
    private Long productId;
    private String name;
    private String description;
    private String brand;
    private BigDecimal basePrice;
    private List<String> imageUrls;
    private String shopName;
    private Integer totalStock; // Tổng tồn kho từ các biến thể
    private String status;

    private List<VariantDTO> variants; // Danh sách biến thể
    private List<ReviewDTO> reviews;   // Danh sách đánh giá

    @Data @Builder
    public static class VariantDTO {
        private Long variantId;
        private String sku;
        private BigDecimal price;
        private Integer stockQuantity;
        private String imageUrl;
    }

    @Data @Builder
    public static class ReviewDTO {
        private Integer rating;
        private String comment;
        private String imageUrl;
        private String userName;
    }
}