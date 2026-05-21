package com.gr6.SmartCart.modules.storefront.repository;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StorefrontProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            SELECT DISTINCT p
            FROM Product p
            WHERE p.status = :status
              AND p.shop.status = :shopStatus
              AND p.category.categoryStatus = :categoryStatus
              AND EXISTS (
                    SELECT 1
                    FROM ProductVariant v
                    WHERE v.product = p
                      AND (v.status = :variantStatus OR v.status IS NULL)
              )
            ORDER BY p.productId DESC
            """)
    List<Product> findTop10SellableProducts(
            @Param("status") ProductStatus status,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("categoryStatus") CategoryStatus categoryStatus,
            @Param("variantStatus") VariantStatus variantStatus,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT p
            FROM Product p
            WHERE p.status = :status
              AND p.shop.status = :shopStatus
              AND p.category.categoryStatus = :categoryStatus
              AND EXISTS (
                    SELECT 1
                    FROM ProductVariant v
                    WHERE v.product = p
                      AND (v.status = :variantStatus OR v.status IS NULL)
              )
              AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR p.category.categoryId = :categoryId)
              AND (
                    :minPrice IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM ProductVariant vMin
                        WHERE vMin.product = p
                          AND (vMin.status = :variantStatus OR vMin.status IS NULL)
                          AND vMin.price >= :minPrice
                    )
              )
              AND (
                    :maxPrice IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM ProductVariant vMax
                        WHERE vMax.product = p
                          AND (vMax.status = :variantStatus OR vMax.status IS NULL)
                          AND vMax.price <= :maxPrice
                    )
              )
            ORDER BY p.productId DESC
            """)
    Page<Product> searchActiveProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") ProductStatus status,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("categoryStatus") CategoryStatus categoryStatus,
            @Param("variantStatus") VariantStatus variantStatus,
            Pageable pageable
    );
}