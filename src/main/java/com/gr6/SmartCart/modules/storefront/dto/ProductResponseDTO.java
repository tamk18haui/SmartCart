package com.gr6.SmartCart.modules.storefront.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductResponseDTO {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String imageUrl;
}