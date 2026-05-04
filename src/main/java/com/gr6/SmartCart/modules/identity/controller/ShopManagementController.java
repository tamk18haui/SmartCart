package com.gr6.SmartCart.modules.identity.controller;

import com.gr6.SmartCart.modules.identity.dto.ShopManagerRequest;
import com.gr6.SmartCart.modules.identity.service.ShopManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopManagementController {
    private final ShopManagerService shopManagerService;

    @PutMapping("/{id}")
    public String updateShop(@PathVariable Integer id, @RequestBody ShopManagerRequest request) {
        return shopManagerService.updateShop(id, request);
    }
}