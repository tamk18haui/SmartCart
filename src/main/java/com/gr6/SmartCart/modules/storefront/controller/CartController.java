package com.gr6.SmartCart.modules.storefront.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.storefront.dto.CartDetailResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.CartItemRequest;
import com.gr6.SmartCart.modules.storefront.dto.ChangeVariantRequest;
import com.gr6.SmartCart.modules.storefront.service.CartService;

@RestController
@RequestMapping("/api/v1/storefront/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Xem giỏ hàng
    @GetMapping("/items")
    public ResponseEntity<BaseResponse<CartDetailResponseDTO>> getCartItems() {
        CartDetailResponseDTO data = cartService.getCartDetails();
        return ResponseEntity.ok(BaseResponse.success_data("Lấy danh sách giỏ hàng thành công", data));
    }

    // Thêm vào giỏ
    @PostMapping("/add")
    public ResponseEntity<BaseResponse<Void>> addToCart(@RequestBody CartItemRequest request) {
        String message = cartService.addToCart(request);
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }

    // Cập nhật số lượng (+ / -)
    @PutMapping("/update")
    public ResponseEntity<BaseResponse<Void>> updateQuantity(@RequestBody CartItemRequest request) {
        String message = cartService.updateQuantity(request);
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }

    // API MỚI: Đổi thuộc tính / biến thể trong giỏ
    @PutMapping("/change-variant")
    public ResponseEntity<BaseResponse<Void>> changeVariant(@RequestBody ChangeVariantRequest request) {
        String message = cartService.changeVariant(request);
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }

    // Xóa item
    @DeleteMapping("/remove/{variantId}")
    public ResponseEntity<BaseResponse<Void>> removeFromCart(@PathVariable Long variantId) {
        String message = cartService.removeFromCart(variantId);
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }
}