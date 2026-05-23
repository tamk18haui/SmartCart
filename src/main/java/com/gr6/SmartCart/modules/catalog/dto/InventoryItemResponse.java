package com.gr6.SmartCart.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemResponse {
    private Long productId;
    private Long variantId;
    private String productName;
    private String sku;
    private String variantName;
    private String imageUrl;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean lowStock;
}
