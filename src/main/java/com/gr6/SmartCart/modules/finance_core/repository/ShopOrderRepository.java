package com.gr6.SmartCart.modules.finance_core.repository;

import com.gr6.SmartCart.common.domain.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {
    @Query("SELECT o FROM ShopOrder o WHERE o.shop.user.email = :email " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "OR CAST(o.shopOrderId AS string) LIKE %:keyword% " +
            "OR LOWER(o.order.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')))") // Lấy từ o.order.receiverName
    List<ShopOrder> searchOrdersByShop(@Param("email") String email, @Param("keyword") String keyword);

}
