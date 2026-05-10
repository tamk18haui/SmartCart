package com.gr6.SmartCart.modules.identity.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.identity.dto.ShopManagerRequest;
import com.gr6.SmartCart.modules.identity.service.ShopManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopManagementController {

    private final ShopManagerService shopManagerService;

    // API: PUT http://localhost:8080/api/v1/shops/update
    @PutMapping("/update")
    public BaseResponse updateShop(@Valid @RequestBody ShopManagerRequest request) {
        return shopManagerService.updateShop(request);
    }
    @GetMapping("/info")
    public BaseResponse getShop() {
        return shopManagerService.getShopInfo();
    }
}