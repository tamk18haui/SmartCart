package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogReviewRepository extends JpaRepository<Review, Long> {

    // Tính trung bình cộng Rating
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product.productId = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
}