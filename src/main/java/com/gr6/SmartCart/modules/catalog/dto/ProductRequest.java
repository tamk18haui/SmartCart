package com.gr6.SmartCart.modules.catalog.dto;

import com.gr6.SmartCart.common.enums.ProductCondition;
import jakarta.validation.constraints.Min;
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
    private ProductCondition condition;

    @NotNull(message = "Giá cơ bản không được để trống")
    @Min(value = 0, message = "Giá không được âm")
    private BigDecimal basePrice;

    @NotNull(message = "Cân nặng không được để trống")
    @Min(value = 0, message = "Cân nặng không được âm")
    private BigDecimal weight;

    @NotNull(message = "Chiều dài không được để trống")
    @Min(value = 0, message = "Chiều dài không được âm")
    private BigDecimal length;

    @NotNull(message = "Chiều rộng không được để trống")
    @Min(value = 0, message = "Chiều rộng không được âm")
    private BigDecimal width;

    @NotNull(message = "Chiều cao không được để trống")
    @Min(value = 0, message = "Chiều cao không được âm")
    private BigDecimal height;

    @NotNull(message = "ID danh mục không được để trống")
    private Long categoryId;

    // THÊM TRƯỜNG NÀY: Để nhận số lượng tồn kho ban đầu từ người bán
    @NotNull(message = "Số lượng tồn kho ban đầu không được để trống")
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stockQuantity;
}