package com.gr6.SmartCart.modules.catalog.dto;

import com.gr6.SmartCart.common.enums.ProductCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;
    private String brand;
    private ProductCondition condition; // NEW hoặc USED

    @NotNull(message = "Giá cơ bản không được để trống")
    private BigDecimal basePrice;

    @NotNull(message = "Cân nặng không được để trống")
    private BigDecimal weight;

    @NotNull(message = "Chiều dài không được để trống")
    private BigDecimal length;

    @NotNull(message = "Chiều rộng không được để trống")
    private BigDecimal width;

    @NotNull(message = "Chiều cao không được để trống")
    private BigDecimal height;

    @NotNull(message = "ID danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "ID Shop không được để trống")
    private Long shopId;
}