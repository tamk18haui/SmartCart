package com.gr6.SmartCart.modules.storefront.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import com.gr6.SmartCart.common.domain.CartItem;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.storefront.dto.CartItemRequest;
import com.gr6.SmartCart.modules.storefront.dto.CartItemResponseDTO;
import com.gr6.SmartCart.modules.storefront.repository.CartItemRepository;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;  // Inject UserRepository để query user từ DB


    // Thêm method helper để lấy userId hiện tại từ SecurityContext
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User chưa đăng nhập");
        }
        String email = auth.getName();  // Lấy email từ authentication 
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user trong DB"));
        return user.getUserId();
    }

    public String addToCart(CartItemRequest request) {
        Long currentUserId = getCurrentUserId();  // Lấy userId động

        // Tìm xem variant này đã có trong giỏ chưa
        CartItem existingItem = cartItemRepository.findByUser_UserIdAndVariant_VariantId(currentUserId, request.getProductId()).orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
            return "Đã cập nhật số lượng sản phẩm trong giỏ.";
        } else {
            CartItem newItem = new CartItem();
            
            User user = new User();
            user.setUserId(currentUserId);  // Sử dụng userId động
            newItem.setUser(user);

            ProductVariant variant = new ProductVariant();
            variant.setVariantId(request.getProductId());
            newItem.setVariant(variant);
            
            newItem.setQuantity(request.getQuantity());
            
            cartItemRepository.save(newItem);
            return "Đã thêm mới sản phẩm vào giỏ hàng.";
        }
    }

    public String updateQuantity(CartItemRequest request) {
        Long currentUserId = getCurrentUserId();  // Lấy userId động

        CartItem item = cartItemRepository.findByUser_UserIdAndVariant_VariantId(currentUserId, request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));
        
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return "Cập nhật thành công";
    }

    public String removeFromCart(Long productId) {
        Long currentUserId = getCurrentUserId();  // Lấy userId động

        CartItem item = cartItemRepository.findByUser_UserIdAndVariant_VariantId(currentUserId, productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        cartItemRepository.delete(item);
        return "Đã xóa khỏi giỏ hàng";
    }

    public List<CartItemResponseDTO> getCartItems() {
        Long currentUserId = getCurrentUserId();
        List<CartItem> cartItems = cartItemRepository.findByUser_UserId(currentUserId);
        return cartItems.stream().map(this::mapToCartItemDTO).collect(Collectors.toList());
    }

    private CartItemResponseDTO mapToCartItemDTO(CartItem item) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setCartItemId(item.getCartItemId());
        dto.setQuantity(item.getQuantity());
        
        ProductVariant variant = item.getVariant();
        if (variant != null) {
            dto.setProductId(variant.getVariantId());
            // Giả sử ProductVariant có reference đến Product
            if (variant.getProduct() != null) {
                dto.setProductName(variant.getProduct().getName());
                dto.setPrice(variant.getPrice() != null ? variant.getPrice().doubleValue() : 0.0);
                // Image: lấy từ product nếu variant không có
                String imageUrls = variant.getProduct().getImageUrls();
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    String[] urls = imageUrls.split(",");
                    dto.setImageUrl(urls[0].trim());
                }
            }
        }
        return dto;
    }
}