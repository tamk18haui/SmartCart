package com.gr6.SmartCart.modules.storefront.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.storefront.dto.CartDetailResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.CartItemRequest;
import com.gr6.SmartCart.modules.storefront.dto.ChangeVariantRequest;
import com.gr6.SmartCart.modules.storefront.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storefront/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/items")
    public ResponseEntity<BaseResponse<CartDetailResponseDTO>> getCartItems() {
        CartDetailResponseDTO data = cartService.getCartDetails();
        return ResponseEntity.ok(BaseResponse.success_data("Lấy danh sách giỏ hàng thành công", data));
    }

    @PostMapping("/add")
    public ResponseEntity<BaseResponse<Void>> addToCart(@Valid @RequestBody CartItemRequest request) {
        String message = cartService.addToCart(request);
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponse<Void>> updateQuantity(@Valid @RequestBody CartItemRequest request) {
        String message = cartService.updateQuantity(request);
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }

    @PutMapping("/change-variant")
    public ResponseEntity<BaseResponse<Void>> changeVariant(@RequestBody ChangeVariantRequest request) {
        String message = cartService.changeVariant(request);
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }

    @DeleteMapping("/remove/{variantId}")
    public ResponseEntity<BaseResponse<Void>> removeFromCart(@PathVariable Long variantId) {
        String message = cartService.removeFromCart(variantId);
        return ResponseEntity.ok(BaseResponse.successMessage(message));
    }
}