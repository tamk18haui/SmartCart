package com.gr6.SmartCart.modules.storefront.controller;

import com.gr6.SmartCart.modules.storefront.dto.SearchFilterRequest;
import com.gr6.SmartCart.modules.storefront.service.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storefront/discovery")
public class DiscoveryController {

    @Autowired
    private DiscoveryService discoveryService;

    // (SMAR-23) API Lấy list sản phẩm ra trang chủ
    @GetMapping("/home-products")
    public ResponseEntity<?> getHomeProducts() {
        return ResponseEntity.ok(discoveryService.getHomeProducts());
    }

    // (SMAR-26) API Tìm kiếm và lọc sản phẩm
    @PostMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestBody SearchFilterRequest request) {
        return ResponseEntity.ok(discoveryService.searchAndFilterProducts(request));
    }
}