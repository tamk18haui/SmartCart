package com.gr6.SmartCart.modules.storefront.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.storefront.dto.ProductResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.SearchFilterRequest;
import com.gr6.SmartCart.modules.storefront.service.DiscoveryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/storefront/discovery")
public class DiscoveryController {

    @Autowired
    private DiscoveryService discoveryService;

    @GetMapping("/home-products")
    public ResponseEntity<BaseResponse<List<ProductResponseDTO>>> getHomeProducts() {
        List<ProductResponseDTO> data = discoveryService.getHomeProducts();

        return ResponseEntity.ok(
                BaseResponse.success_data(
                        "Lấy danh sách sản phẩm trang chủ thành công",
                        data
                )
        );
    }

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<Page<ProductResponseDTO>>> searchProducts(
            @RequestBody(required = false) SearchFilterRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProductResponseDTO> data = discoveryService.searchAndFilterProducts(
                request,
                page,
                size
        );

        return ResponseEntity.ok(
                BaseResponse.success_data(
                        "Tìm kiếm thành công",
                        data
                )
        );
    }
}