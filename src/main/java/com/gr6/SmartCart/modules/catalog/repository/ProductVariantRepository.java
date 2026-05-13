package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.enums.VariantStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    boolean existsByProductProductIdAndSku(Long productId, String sku);

    boolean existsByProductProductIdAndSkuAndVariantIdNot(Long productId, String sku, Long variantId);

    List<ProductVariant> findByProductProductId(Long productId);

    List<ProductVariant> findByProductProductIdAndStatusNot(Long productId, VariantStatus status);

    Optional<ProductVariant> findByVariantIdAndProductShopShopIdAndStatusNot(
            Long variantId,
            Long shopId,
            VariantStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.variantId = :variantId")
    Optional<ProductVariant> findByIdWithLock(@Param("variantId") Long variantId);
}