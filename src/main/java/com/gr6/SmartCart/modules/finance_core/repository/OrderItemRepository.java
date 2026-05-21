package com.gr6.SmartCart.modules.finance_core.repository;

import com.gr6.SmartCart.common.domain.OrderItem;
import com.gr6.SmartCart.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @EntityGraph(attributePaths = {
            "shopOrder",
            "shopOrder.order",
            "shopOrder.shop",
            "variant",
            "variant.product",
            "variant.product.shop"
    })
    @Query("""
            SELECT oi
            FROM OrderItem oi
            WHERE oi.shopOrder.order.user.email = :email
            AND oi.shopOrder.status = :status
            AND NOT EXISTS (
                SELECT r
                FROM Review r
                WHERE r.orderItem.orderItemId = oi.orderItemId
            )
            ORDER BY oi.orderItemId DESC
            """)
    List<OrderItem> findReviewableItemsByBuyerEmail(
            @Param("email") String email,
            @Param("status") OrderStatus status
    );
}