package com.gr6.SmartCart.modules.fulfillment.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.ProductDetailResponse;
import com.gr6.SmartCart.modules.fulfillment.service.ProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fulfillment")
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductDetailService productDetailService;

    @GetMapping("/product/{id}")
    public BaseResponse<ProductDetailResponse> getProductDetail(@PathVariable Long id) {
        return productDetailService.getProductDetail(id);
    }
}