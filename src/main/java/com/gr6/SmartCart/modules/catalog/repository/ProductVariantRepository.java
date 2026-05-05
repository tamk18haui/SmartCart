package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.variantId = :variantId")
    Optional<ProductVariant> findByIdWithLock(@Param("variantId") Long variantId);

    // Lệnh mới: Kiểm tra xem SKU đã có ai dùng chưa
    boolean existsBySku(String sku);
}