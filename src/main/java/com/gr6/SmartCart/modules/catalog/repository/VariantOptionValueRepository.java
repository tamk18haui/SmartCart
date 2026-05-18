package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.VariantOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantOptionValueRepository extends JpaRepository<VariantOptionValue, Long> {

    void deleteByVariantVariantId(Long variantId);
}