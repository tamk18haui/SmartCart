package com.gr6.SmartCart.modules.storefront.service;

import com.gr6.SmartCart.common.domain.CartItem;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.modules.storefront.dto.CartItemRequest;
import com.gr6.SmartCart.modules.storefront.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    private final Long CURRENT_USER_ID = 1L; // Tạm thời hardcode User ID là 1 để test

    public String addToCart(CartItemRequest request) {
        // Tìm xem variant này đã có trong giỏ chưa
        CartItem existingItem = cartItemRepository.findByUser_UserIdAndVariant_VariantId(CURRENT_USER_ID, request.getProductId()).orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
            return "Đã cập nhật số lượng sản phẩm trong giỏ.";
        } else {
            CartItem newItem = new CartItem();
            
            User user = new User();
            user.setUserId(CURRENT_USER_ID); 
            newItem.setUser(user);

            ProductVariant variant = new ProductVariant();
            variant.setVariantId(request.getProductId()); // ID từ request gửi lên gán vào variantId
            newItem.setVariant(variant);
            
            newItem.setQuantity(request.getQuantity());
            
            cartItemRepository.save(newItem);
            return "Đã thêm mới sản phẩm vào giỏ hàng.";
        }
    }

    public String updateQuantity(CartItemRequest request) {
        CartItem item = cartItemRepository.findByUser_UserIdAndVariant_VariantId(CURRENT_USER_ID, request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));
        
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return "Cập nhật thành công";
    }

    public String removeFromCart(Long productId) {
        CartItem item = cartItemRepository.findByUser_UserIdAndVariant_VariantId(CURRENT_USER_ID, productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        cartItemRepository.delete(item);
        return "Đã xóa khỏi giỏ hàng";
    }
}