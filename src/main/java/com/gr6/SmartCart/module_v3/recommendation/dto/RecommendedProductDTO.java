package com.gr6.SmartCart.module_v3.recommendation.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedProductDTO {
    private Long productId;
    private String name;
    private String imageUrl;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String shopName;
    private Long shopId;
    private String categoryName;
    private Integer soldCount;
    private Double averageRating;
    private Long reviewCount;
    private Double score;
    private String reason;
}
