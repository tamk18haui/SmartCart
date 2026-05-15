package com.gr6.SmartCart.modules.storefront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProductResponse {

    private Long productId;
    private Long shopId;
    private String shopName;

    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private String imageUrl;

    private Integer soldQuantity;
    private Double ratingAverage;
    private Integer reviewCount;

    private String status;
}