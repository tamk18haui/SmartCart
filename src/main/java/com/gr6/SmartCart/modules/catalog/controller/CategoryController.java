package com.gr6.SmartCart.modules.catalog.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.catalog.dto.CategoryRequest;
import com.gr6.SmartCart.modules.catalog.dto.CategoryResponse;
import com.gr6.SmartCart.modules.catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public BaseResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @GetMapping
    public BaseResponse<List<CategoryResponse>> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/admin")
    public BaseResponse<List<CategoryResponse>> getAllCategoriesForAdmin(){return categoryService.getAllCategoriesForAdmin();}

    // SÁNG THÊM VÀO ĐÂY: Sửa danh mục
    @PutMapping("/{id}")
    public BaseResponse<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    // SÁNG THÊM VÀO ĐÂY: Ẩn/Hiện danh mục thay vì xóa
    @PatchMapping("/{id}/toggle-status")
    public BaseResponse<String> toggleCategoryStatus(@PathVariable Long id) {
        return categoryService.toggleCategoryStatus(id);
    }
}