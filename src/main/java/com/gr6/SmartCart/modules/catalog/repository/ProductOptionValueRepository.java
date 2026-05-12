package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {

    Optional<ProductOptionValue> findByProductOptionProductOptionIdAndValueIgnoreCase(
            Long productOptionId,
            String value
    );
}