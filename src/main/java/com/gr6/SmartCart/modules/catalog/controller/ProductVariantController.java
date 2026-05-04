package com.gr6.SmartCart.modules.catalog.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.modules.catalog.dto.VariantCreateRequest;
import com.gr6.SmartCart.modules.catalog.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping
    public BaseResponse<ProductVariant> createVariant(@RequestBody VariantCreateRequest request) {
        return variantService.createVariant(request);
    }
}