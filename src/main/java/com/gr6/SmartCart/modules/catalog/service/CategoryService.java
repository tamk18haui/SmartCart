package com.gr6.SmartCart.modules.catalog.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.catalog.dto.CategoryRequest;
import com.gr6.SmartCart.modules.catalog.dto.CategoryResponse;
import java.util.List;

public interface CategoryService {
    BaseResponse<CategoryResponse> createCategory(CategoryRequest request);
    BaseResponse<List<CategoryResponse>> getAllCategories();

    // SÁNG THÊM VÀO ĐÂY:
    BaseResponse<CategoryResponse> updateCategory(Long id, CategoryRequest request);
    BaseResponse<String> toggleCategoryStatus(Long id);
    BaseResponse<List<CategoryResponse>> getAllCategoriesForAdmin();
}