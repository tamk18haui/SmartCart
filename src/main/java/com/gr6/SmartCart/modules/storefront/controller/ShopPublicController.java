package com.gr6.SmartCart.modules.storefront.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.modules.storefront.dto.ShopProductResponse;
import com.gr6.SmartCart.modules.storefront.dto.ShopPublicResponse;
import com.gr6.SmartCart.modules.storefront.service.ShopPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storefront/shops")
@RequiredArgsConstructor
public class ShopPublicController {

    private final ShopPublicService shopPublicService;

    @GetMapping("/{shopId}")
    public BaseResponse<ShopPublicResponse> getShopDetail(@PathVariable Long shopId) {
        return shopPublicService.getShopDetail(shopId);
    }

    @GetMapping("/{shopId}/products")
    public BaseResponse<PageResponse<ShopProductResponse>> getShopProducts(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return shopPublicService.getShopProducts(shopId, page, size);
    }
}