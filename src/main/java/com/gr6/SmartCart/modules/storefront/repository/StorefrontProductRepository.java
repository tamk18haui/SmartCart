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
            SELECT p
            FROM Product p
            JOIN p.shop s
            JOIN p.category c
            WHERE p.status = :productStatus
              AND s.status = :shopStatus
              AND c.categoryStatus = :categoryStatus
              AND EXISTS (
                    SELECT 1
                    FROM ProductVariant v
                    WHERE v.product = p
                      AND v.status = :variantStatus
                      AND COALESCE(v.stockQuantity, 0) > 0
              )
            ORDER BY p.productId DESC
            """)
    List<Product> findTop10SellableProducts(
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("categoryStatus") CategoryStatus categoryStatus,
            @Param("variantStatus") VariantStatus variantStatus,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT p
                    FROM Product p
                    JOIN p.shop s
                    JOIN p.category c
                    WHERE p.status = :productStatus
                      AND s.status = :shopStatus
                      AND c.categoryStatus = :categoryStatus

                      AND (:categoryId IS NULL OR c.categoryId = :categoryId)

                      AND (
                            :keyword IS NULL
                            OR :keyword = ''
                            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(COALESCE(p.brand, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )

                      AND EXISTS (
                            SELECT 1
                            FROM ProductVariant v
                            WHERE v.product = p
                              AND v.status = :variantStatus
                              AND COALESCE(v.stockQuantity, 0) > 0
                              AND (:minPrice IS NULL OR v.price >= :minPrice)
                              AND (:maxPrice IS NULL OR v.price <= :maxPrice)
                      )

                    ORDER BY
                      CASE
                        WHEN :sortBy = 'relevance'
                             AND LOWER(p.name) = LOWER(:keyword)
                        THEN 0

                        WHEN :sortBy = 'relevance'
                             AND LOWER(p.name) LIKE LOWER(CONCAT(:keyword, '%'))
                        THEN 1

                        WHEN :sortBy = 'relevance'
                             AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        THEN 2

                        WHEN :sortBy = 'relevance'
                             AND LOWER(COALESCE(p.brand, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        THEN 3

                        WHEN :sortBy = 'relevance'
                             AND LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        THEN 4

                        ELSE 5
                      END ASC,

                      CASE
                        WHEN :sortBy = 'newest'
                        THEN p.productId
                      END DESC,

                      CASE
                        WHEN :sortBy = 'sold_desc'
                        THEN COALESCE(p.soldCount, 0)
                      END DESC,

                      CASE
                        WHEN :sortBy = 'price_asc'
                        THEN (
                            SELECT MIN(vp.price)
                            FROM ProductVariant vp
                            WHERE vp.product = p
                              AND vp.status = :variantStatus
                              AND COALESCE(vp.stockQuantity, 0) > 0
                        )
                      END ASC,

                      CASE
                        WHEN :sortBy = 'price_desc'
                        THEN (
                            SELECT MIN(vp.price)
                            FROM ProductVariant vp
                            WHERE vp.product = p
                              AND vp.status = :variantStatus
                              AND COALESCE(vp.stockQuantity, 0) > 0
                        )
                      END DESC,

                      p.productId DESC
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Product p
                    JOIN p.shop s
                    JOIN p.category c
                    WHERE p.status = :productStatus
                      AND s.status = :shopStatus
                      AND c.categoryStatus = :categoryStatus

                      AND (:categoryId IS NULL OR c.categoryId = :categoryId)

                      AND (
                            :keyword IS NULL
                            OR :keyword = ''
                            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(COALESCE(p.brand, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )

                      AND EXISTS (
                            SELECT 1
                            FROM ProductVariant v
                            WHERE v.product = p
                              AND v.status = :variantStatus
                              AND COALESCE(v.stockQuantity, 0) > 0
                              AND (:minPrice IS NULL OR v.price >= :minPrice)
                              AND (:maxPrice IS NULL OR v.price <= :maxPrice)
                      )
                    """
    )
    Page<Product> searchActiveProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("sortBy") String sortBy,
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("categoryStatus") CategoryStatus categoryStatus,
            @Param("variantStatus") VariantStatus variantStatus,
            Pageable pageable
    );
}