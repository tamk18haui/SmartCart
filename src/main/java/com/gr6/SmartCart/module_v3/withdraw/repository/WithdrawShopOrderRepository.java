package com.gr6.SmartCart.module_v3.withdraw.repository;

import com.gr6.SmartCart.common.domain.ShopOrder;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WithdrawShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    @Query("""
            SELECT so FROM ShopOrder so
            JOIN FETCH so.shop s
            JOIN FETCH s.user seller
            JOIN FETCH so.order o
            WHERE so.status = com.gr6.SmartCart.common.enums.OrderStatus.COMPLETED
            AND NOT EXISTS (
                SELECT 1 FROM SellerSettlement ss
                WHERE ss.shopOrder = so
            )
            ORDER BY so.shopOrderId ASC
            """)
    List<ShopOrder> findCompletedUnsettledShopOrders();

    @Query("""
            SELECT so FROM ShopOrder so
            JOIN FETCH so.shop s
            JOIN FETCH s.user seller
            JOIN FETCH so.order o
            WHERE so.shop.shopId = :shopId
            AND so.status IN (
                com.gr6.SmartCart.common.enums.OrderStatus.PENDING,
                com.gr6.SmartCart.common.enums.OrderStatus.CONFIRMED,
                com.gr6.SmartCart.common.enums.OrderStatus.PREPARING,
                com.gr6.SmartCart.common.enums.OrderStatus.SHIPPING,
                com.gr6.SmartCart.common.enums.OrderStatus.DELIVERED,
                com.gr6.SmartCart.common.enums.OrderStatus.COMPLETED
            )
            AND NOT EXISTS (
                SELECT 1 FROM SellerSettlement ss
                WHERE ss.shopOrder = so
            )
            ORDER BY so.shopOrderId ASC
            """)
    List<ShopOrder> findSellerPayableUnsettledShopOrders(@Param("shopId") Long shopId);
}
