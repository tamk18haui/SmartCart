package com.gr6.SmartCart.modules.catalog.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class VariantCreateRequest {
    private Long productId;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;

    // Ví dụ: {"Màu sắc": "Đỏ", "Kích cỡ": "L"}
    private Map<String, String> attributes;
}