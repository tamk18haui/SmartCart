package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.OrderItem;
import com.gr6.SmartCart.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogOrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            SELECT COALESCE(SUM(oi.quantity), 0)
            FROM OrderItem oi
            WHERE oi.variant.product.productId = :productId
              AND oi.shopOrder.status = :status
            """)
    Integer getSoldQuantityByProductId(
            @Param("productId") Long productId,
            @Param("status") OrderStatus status
    );

    @Query("""
            SELECT COALESCE(SUM(oi.quantity), 0)
            FROM OrderItem oi
            WHERE oi.variant.product.productId = :productId
              AND oi.shopOrder.status IN :statuses
            """)
    Integer getSoldQuantityByProductId(
            @Param("productId") Long productId,
            @Param("statuses") List<OrderStatus> statuses
    );
}