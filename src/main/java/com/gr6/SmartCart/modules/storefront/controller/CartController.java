package com.gr6.SmartCart.modules.storefront.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.storefront.dto.CartItemRequest;
import com.gr6.SmartCart.modules.storefront.dto.CartItemResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import com.gr6.SmartCart.modules.storefront.service.CartService;

@RestController
@RequestMapping("/api/storefront/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/items")
    public ResponseEntity<BaseResponse<List<CartItemResponseDTO>>> getCartItems() {
        List<CartItemResponseDTO> data = cartService.getCartItems();
        return ResponseEntity.ok(BaseResponse.success_data("Lấy danh sách giỏ hàng thành công", data));
    }

    @PostMapping("/add")
    public ResponseEntity<BaseResponse<Void>> addToCart(@RequestBody CartItemRequest request) {
        String message = cartService.addToCart(request);
        // Dùng successMessage vì chỉ cần báo trạng thái thành công
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponse<Void>> updateQuantity(@RequestBody CartItemRequest request) {
        String message = cartService.updateQuantity(request);
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<BaseResponse<Void>> removeFromCart(@PathVariable Long productId) {
        String message = cartService.removeFromCart(productId);
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }
}