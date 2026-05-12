package com.gr6.SmartCart.modules.catalog.dto;

import com.gr6.SmartCart.common.enums.ProductCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;
    private String brand;

    @NotNull(message = "Tình trạng sản phẩm không được để trống")
    private ProductCondition condition;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "1.0", message = "Giá phải lớn hơn 0")
    private BigDecimal basePrice;

    @NotNull(message = "Cân nặng không được để trống")
    @DecimalMin(value = "0.01", message = "Cân nặng phải lớn hơn 0")
    private BigDecimal weight;

    @DecimalMin(value = "0.01", message = "Chiều dài phải lớn hơn 0")
    private BigDecimal length;

    @DecimalMin(value = "0.01", message = "Chiều rộng phải lớn hơn 0")
    private BigDecimal width;

    @DecimalMin(value = "0.01", message = "Chiều cao phải lớn hơn 0")
    private BigDecimal height;

    @NotNull(message = "Số lượng kho không được để trống")
    private Integer stockQuantity;

    private List<String> uploadImages;
}