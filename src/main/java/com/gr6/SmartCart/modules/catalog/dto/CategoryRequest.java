package com.gr6.SmartCart.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống!")
    private String categoryName;
    private String categoryDescription;
    private String categoryImageUrl;
}