package com.gr6.SmartCart.modules.storefront.repository;

import com.gr6.SmartCart.common.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Spring Data JPA sẽ tự hiểu: Vào bảng CartItem -> Tìm user -> Lấy userId VÀ Tìm variant -> Lấy variantId
    Optional<CartItem> findByUser_UserIdAndVariant_VariantId(Long userId, Long variantId);
    
    List<CartItem> findByUser_UserId(Long userId);
}