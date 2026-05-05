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
}