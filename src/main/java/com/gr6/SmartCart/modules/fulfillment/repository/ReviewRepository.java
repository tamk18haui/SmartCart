package com.gr6.SmartCart.modules.fulfillment.repository;

import com.gr6.SmartCart.common.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Lấy đánh giá theo ProductId [cite: 580]
    @Query("SELECT r FROM Review r WHERE r.product.productId = :productId")
    List<Review> findByProductId(Long productId);
}
