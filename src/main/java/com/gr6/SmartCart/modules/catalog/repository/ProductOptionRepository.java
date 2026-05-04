package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
}