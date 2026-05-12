package com.gr6.SmartCart.modules.storefront.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gr6.SmartCart.common.domain.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    Optional<CartItem> findByUser_UserIdAndVariant_VariantId(Long userId, Long variantId);
    
    List<CartItem> findByUser_UserId(Long userId);

    // THÊM MỚI: Tìm chính xác 1 item trong giỏ của người dùng cụ thể
    Optional<CartItem> findByCartItemIdAndUser_UserId(Long cartItemId, Long userId);

    void deleteByUser_UserIdAndVariant_VariantIdIn(Long userId, Collection<Long> variantIds);

}