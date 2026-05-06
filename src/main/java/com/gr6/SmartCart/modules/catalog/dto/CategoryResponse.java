package com.gr6.SmartCart.modules.catalog.dto;

import com.gr6.SmartCart.common.domain.Category;
import lombok.Data;

@Data
public class CategoryResponse {
    private Long categoryId;
    private String categoryName;
    private String categoryDescription;
    private String categoryImageUrl;
    private String categoryStatus;

    public static CategoryResponse fromEntity(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setCategoryId(category.getCategoryId());
        response.setCategoryName(category.getCategoryName());
        response.setCategoryDescription(category.getCategoryDescription());
        response.setCategoryImageUrl(category.getCategoryImageUrl());
        if (category.getCategoryStatus() != null) {
            response.setCategoryStatus(category.getCategoryStatus().name());
        }
        return response;
    }
}