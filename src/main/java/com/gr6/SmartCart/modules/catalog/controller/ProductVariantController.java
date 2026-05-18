package com.gr6.SmartCart.modules.catalog.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.catalog.dto.VariantCreateRequest;
import com.gr6.SmartCart.modules.catalog.dto.VariantResponse;
import com.gr6.SmartCart.modules.catalog.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
public class ProductVariantController {
    private final ProductVariantService variantService;

    @PostMapping
    public BaseResponse<VariantResponse> createVariant(@Valid @RequestBody VariantCreateRequest request) {
        return variantService.createVariant(request);
    }

    // THÊM: API Cập nhật biến thể
    @PutMapping("/{variantId}")
    public BaseResponse<VariantResponse> updateVariant(@PathVariable Long variantId, @Valid @RequestBody VariantCreateRequest request) {
        return variantService.updateVariant(variantId, request);
    }

    // THÊM: API Xóa biến thể (Để Frontend xóa cái Mặc định đi)
    @DeleteMapping("/{variantId}")
    public BaseResponse<String> deleteVariant(@PathVariable Long variantId) {
        return variantService.deleteVariant(variantId);
    }
}