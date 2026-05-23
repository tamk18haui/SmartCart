package com.gr6.SmartCart.modules.catalog.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.modules.catalog.dto.ProductRequest;
import com.gr6.SmartCart.modules.catalog.dto.ProductResponse;
import com.gr6.SmartCart.modules.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public BaseResponse<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @GetMapping("/brands")
    public BaseResponse<List<String>> getBrandSuggestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        return productService.getBrandSuggestions(keyword, categoryId);
    }

    @GetMapping("/shop/{shopId}")
    public BaseResponse<PageResponse<ProductResponse>> getProductsByShop(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return productService.getProductsByShop(shopId, page, size);
    }

    @GetMapping("/seller/{productId}")
    public BaseResponse<ProductResponse> getProductForSeller(@PathVariable Long productId) {
        return productService.getProductForSeller(productId);
    }

    @PutMapping("/{productId}")
    public BaseResponse<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request
    ) {
        return productService.updateProduct(productId, request);
    }

    @PatchMapping("/{productId}/visibility")
    public BaseResponse<ProductResponse> toggleProductVisibility(
            @PathVariable Long productId,
            @RequestParam boolean hidden
    ) {
        return productService.toggleProductVisibility(productId, hidden);
    }

    @DeleteMapping("/{productId}")
    public BaseResponse<String> deleteProduct(@PathVariable Long productId) {
        return productService.deleteProduct(productId);
    }
}