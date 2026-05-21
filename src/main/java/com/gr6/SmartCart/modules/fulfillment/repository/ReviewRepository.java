package com.gr6.SmartCart.modules.fulfillment.repository;

import com.gr6.SmartCart.common.domain.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {
            "user",
            "product",
            "product.shop",
            "order",
            "orderItem",
            "orderItem.variant"
    })
    @Query("""
            SELECT r
            FROM Review r
            WHERE r.product.productId = :productId
            ORDER BY r.createdAt DESC
            """)
    List<Review> findByProductId(@Param("productId") Long productId);

    @EntityGraph(attributePaths = {
            "user",
            "product",
            "product.shop",
            "order",
            "orderItem",
            "orderItem.variant"
    })
    Optional<Review> findByOrderItem_OrderItemId(Long orderItemId);

    boolean existsByOrderItem_OrderItemId(Long orderItemId);

    @EntityGraph(attributePaths = {
            "user",
            "product",
            "product.shop",
            "order",
            "orderItem",
            "orderItem.variant"
    })
    List<Review> findByUser_EmailOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = {
            "user",
            "product",
            "product.shop",
            "order",
            "orderItem",
            "orderItem.variant"
    })
    List<Review> findByProduct_Shop_User_EmailOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = {
            "user",
            "product",
            "product.shop",
            "order",
            "orderItem",
            "orderItem.variant"
    })
    List<Review> findAllByOrderByCreatedAtDesc();
}