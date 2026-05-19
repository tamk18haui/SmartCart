package com.gr6.SmartCart.module_v3.withdraw.repository;

import com.gr6.SmartCart.common.domain.ShopOrder;
import org.springframework.data.jpa.repository.*;
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
}