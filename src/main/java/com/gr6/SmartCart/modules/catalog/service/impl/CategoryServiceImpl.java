package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Category;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.modules.catalog.dto.CategoryRequest;
import com.gr6.SmartCart.modules.catalog.repository.CategoryRepository;
import com.gr6.SmartCart.modules.catalog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public BaseResponse<Category> createCategory(CategoryRequest request) {
        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            return BaseResponse.error(400, "Tên danh mục này đã tồn tại trong hệ thống!");
        }

        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setCategoryDescription(request.getCategoryDescription());
        category.setCategoryImageUrl(request.getCategoryImageUrl());
        category.setCategoryStatus(CategoryStatus.ACTIVE);

        Category savedCategory = categoryRepository.save(category);

        return BaseResponse.success_data("Tạo danh mục thành công!", savedCategory);
    }

    @Override
    public BaseResponse<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return BaseResponse.success_data("Lấy danh sách danh mục thành công", categories);
    }
}