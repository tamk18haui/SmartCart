package com.gr6.SmartCart.modules.catalog.dto;

import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.VariantOptionValue;
import lombok.Data;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class VariantResponse {
    private Long variantId;
    private Long productId;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;

    // THÊM: Trả về chi tiết Màu sắc, Size... cho Seller nhìn
    private Map<String, String> attributes;

    public static VariantResponse fromEntity(ProductVariant variant) {
        VariantResponse response = new VariantResponse();
        response.setVariantId(variant.getVariantId());
        response.setProductId(variant.getProduct().getProductId());
        response.setSku(variant.getSku());
        response.setPrice(variant.getPrice());
        response.setStockQuantity(variant.getStockQuantity());
        response.setImageUrl(variant.getImageUrl());

        // Lôi các Option (Màu, Size) từ Database ra và nhét vào Map
        if (variant.getVariantOptionValues() != null) {
            Map<String, String> attrs = new HashMap<>();
            for (VariantOptionValue link : variant.getVariantOptionValues()) {
                if (link.getOptionValue() != null && link.getOptionValue().getProductOption() != null) {
                    String optionName = link.getOptionValue().getProductOption().getName();
                    String optionValue = link.getOptionValue().getValue();
                    attrs.put(optionName, optionValue);
                }
            }
            response.setAttributes(attrs);
        }

        return response;
    }
}