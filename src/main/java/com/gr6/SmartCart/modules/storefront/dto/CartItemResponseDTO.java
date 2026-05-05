package com.gr6.SmartCart.modules.storefront.dto;

import lombok.Data;

@Data
public class CartItemResponseDTO {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Double price;
    private Integer quantity;
}