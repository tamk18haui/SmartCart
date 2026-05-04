package com.gr6.SmartCart.modules.catalog.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.modules.catalog.dto.ProductRequest;
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

    // API để Seller đăng bán sản phẩm mới
    @PostMapping
    public BaseResponse<Product> createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    // API xem danh sách sản phẩm của một Shop
    @GetMapping("/shop/{shopId}")
    public BaseResponse<List<Product>> getProductsByShop(@PathVariable Long shopId) {
        return productService.getProductsByShop(shopId);
    }

    // API để Seller xóa sản phẩm
    @DeleteMapping("/{productId}")
    public BaseResponse<String> deleteProduct(@PathVariable Long productId) {
        return productService.deleteProduct(productId);
    }
}