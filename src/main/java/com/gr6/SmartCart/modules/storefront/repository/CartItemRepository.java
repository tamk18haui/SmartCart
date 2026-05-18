package com.gr6.SmartCart.modules.storefront.repository;

import com.gr6.SmartCart.common.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser_UserId(Long userId);

    Optional<CartItem> findByUser_UserIdAndVariant_VariantId(Long userId, Long variantId);

    Optional<CartItem> findByCartItemIdAndUser_UserId(Long cartItemId, Long userId);

    // BỔ SUNG HÀM NÀY ĐỂ XÓA SẢN PHẨM KHỎI GIỎ HÀNG SAU KHI ĐẶT ĐƠN
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.userId = :userId AND c.variant.variantId IN :variantIds")
    void deleteByUser_UserIdAndVariant_VariantIdIn(@Param("userId") Long userId, @Param("variantIds") Set<Long> variantIds);
}