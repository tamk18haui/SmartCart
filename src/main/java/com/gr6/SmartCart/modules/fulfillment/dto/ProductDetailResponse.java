package com.gr6.SmartCart.modules.fulfillment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    private Integer totalStock;
    private String status;

    // --- PHẦN QUAN TRỌNG ĐỂ VẼ NÚT CHỌN MÀU/SIZE ---
    private List<OptionGroupDTO> optionGroups;
    private List<VariantDTO> variants;
    private List<ReviewDTO> reviews;

    @Data @Builder
    public static class OptionGroupDTO {
        private String name; // Ví dụ: "Màu sắc", "Kích cỡ"
        private List<String> values; // Ví dụ: ["Đỏ", "Xanh"], ["S", "M"]
    }

    @Data @Builder
    public static class VariantDTO {
        private Long variantId;
        private String sku;
        private BigDecimal price;
        private Integer stockQuantity;
        private String imageUrl;
        private Map<String, String> attributes; // Ví dụ: {"Màu sắc": "Đỏ", "Size": "S"}
    }

    @Data @Builder
    public static class ReviewDTO {
        private Integer rating;
        private String comment;
        private String userName;
    }
}