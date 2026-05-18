package com.gr6.SmartCart.modules.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class VariantCreateRequest {
    @NotNull(message = "ID sản phẩm gốc không được để trống")
    private Long productId;

    @NotBlank(message = "Mã SKU không được để trống")
    private String sku;

    @NotNull(message = "Giá biến thể không được để trống")
    @Min(value = 0, message = "Giá không được âm")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stockQuantity;

    private String imageUrl;
    private Map<String, String> attributes;
}