package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.Review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
            SELECT COALESCE(AVG(r.rating), 0.0)
            FROM Review r
            WHERE r.product.productId = :productId
            """)
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("""
            SELECT COUNT(r)
            FROM Review r
            WHERE r.product.productId = :productId
            """)
    Integer getReviewCountByProductId(@Param("productId") Long productId);
}