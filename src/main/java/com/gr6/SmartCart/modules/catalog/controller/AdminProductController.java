package com.gr6.SmartCart.modules.catalog.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.modules.catalog.dto.ProductResponse;
import com.gr6.SmartCart.modules.catalog.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public BaseResponse<PageResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String keyword) {
        return adminProductService.getProducts(page, size, status, keyword);
    }

    @PatchMapping("/{productId}/ban")
    public BaseResponse<String> banProduct(@PathVariable Long productId, @RequestParam(required = false) String reason) {
        return adminProductService.banProduct(productId, reason);
    }

    @PatchMapping("/{productId}/unban")
    public BaseResponse<String> unbanProduct(@PathVariable Long productId) {
        return adminProductService.unbanProduct(productId);
    }
}