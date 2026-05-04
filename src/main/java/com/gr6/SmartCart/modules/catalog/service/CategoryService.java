package com.gr6.SmartCart.modules.catalog.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Category;
import com.gr6.SmartCart.modules.catalog.dto.CategoryRequest;
import java.util.List;

public interface CategoryService {
    BaseResponse<Category> createCategory(CategoryRequest request);
    BaseResponse<List<Category>> getAllCategories();
}