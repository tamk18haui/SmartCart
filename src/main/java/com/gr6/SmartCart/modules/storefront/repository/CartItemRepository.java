package com.gr6.SmartCart.modules.storefront.repository;

import com.gr6.SmartCart.common.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Lấy toàn bộ sản phẩm trong giỏ hàng của một User
    List<CartItem> findByUser_UserId(Long userId);

    // Tìm một sản phẩm cụ thể trong giỏ hàng dựa vào User ID và Variant ID
    Optional<CartItem> findByUser_UserIdAndVariant_VariantId(Long userId, Long variantId);

    // Tìm chi tiết một CartItem cụ thể để đảm bảo nó thuộc về đúng User đang thao tác
    Optional<CartItem> findByCartItemIdAndUser_UserId(Long cartItemId, Long userId);
}