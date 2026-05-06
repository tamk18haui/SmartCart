package com.gr6.SmartCart.modules.catalog.dto;

import com.gr6.SmartCart.common.domain.ProductVariant;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class VariantResponse {
    private Long variantId;
    private Long productId;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;

    public static VariantResponse fromEntity(ProductVariant variant) {
        VariantResponse response = new VariantResponse();
        response.setVariantId(variant.getVariantId());
        response.setProductId(variant.getProduct().getProductId());
        response.setSku(variant.getSku());
        response.setPrice(variant.getPrice());
        response.setStockQuantity(variant.getStockQuantity());
        response.setImageUrl(variant.getImageUrl());
        return response;
    }
}