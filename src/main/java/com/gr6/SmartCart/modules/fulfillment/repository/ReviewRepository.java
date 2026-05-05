package com.gr6.SmartCart.modules.fulfillment.repository;

import com.gr6.SmartCart.common.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductProductId(Integer productId);
}
