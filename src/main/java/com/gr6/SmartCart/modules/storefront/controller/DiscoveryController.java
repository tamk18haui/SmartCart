package com.gr6.SmartCart.modules.storefront.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.storefront.dto.ProductResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.SearchFilterRequest;
import com.gr6.SmartCart.modules.storefront.service.DiscoveryService;

@RestController
@RequestMapping("/api/storefront/discovery")
public class DiscoveryController {

    @Autowired
    private DiscoveryService discoveryService;

    @GetMapping("/home-products")
    public ResponseEntity<BaseResponse<List<ProductResponseDTO>>> getHomeProducts() {
        List<ProductResponseDTO> data = discoveryService.getHomeProducts();
        // Gọi hàm success_data với đúng thứ tự (message, data)
        return ResponseEntity.ok(BaseResponse.success_data("Lấy danh sách sản phẩm trang chủ thành công", data));
    }

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<List<ProductResponseDTO>>> searchProducts(@RequestBody SearchFilterRequest request) {
        List<ProductResponseDTO> data = discoveryService.searchAndFilterProducts(request);
        return ResponseEntity.ok(BaseResponse.success_data("Tìm kiếm thành công", data));
    }
}