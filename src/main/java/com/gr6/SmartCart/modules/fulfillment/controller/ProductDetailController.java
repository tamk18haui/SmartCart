package com.gr6.SmartCart.modules.fulfillment.controller;

import com.gr6.SmartCart.modules.fulfillment.service.impl.ProductDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fulfillment")
public class ProductDetailController {
    @Autowired
    private ProductDetailServiceImpl productDetailService;

    @GetMapping("/product/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productDetailService.getProductDetail(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage()); // Xử lý lỗi CSDL hoặc không thấy sản phẩm [cite: 584]
        }
    }

}