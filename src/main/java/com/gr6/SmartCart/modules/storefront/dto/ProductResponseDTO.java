package com.gr6.SmartCart.modules.storefront.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponseDTO {

    private Long productId;

    private Long categoryId;
    private String categoryName;

    private Long shopId;
    private String shopName;

    private String name;

    private BigDecimal price;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal originalPrice;

    private String imageUrl;

    private Integer soldQuantity;
    private Double averageRating;
    private Integer reviewCount;

    private String location;
}