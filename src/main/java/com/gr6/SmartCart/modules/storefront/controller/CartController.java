package com.gr6.SmartCart.modules.storefront.controller;

import com.gr6.SmartCart.modules.storefront.dto.CartItemRequest;
import com.gr6.SmartCart.modules.storefront.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storefront/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // (SMAR-35) Thêm sản phẩm vào giỏ
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    // Sửa số lượng sản phẩm
    @PutMapping("/update")
    public ResponseEntity<?> updateCartItem(@RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateQuantity(request));
    }

    // Xóa sản phẩm khỏi giỏ
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeFromCart(productId));
    }
}