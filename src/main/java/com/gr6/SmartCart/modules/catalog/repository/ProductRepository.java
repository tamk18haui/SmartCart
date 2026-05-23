package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByShopShopIdAndStatusNot(
            Long shopId,
            ProductStatus status,
            Pageable pageable
    );

    Optional<Product> findByProductIdAndShopShopIdAndStatusNot(
            Long productId,
            Long shopId,
            ProductStatus status
    );

    @EntityGraph(attributePaths = {"shop", "category", "variants"})
    @Query("SELECT p FROM Product p " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "AND (:shopId IS NULL OR p.shop.shopId = :shopId) " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.shop.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.productId DESC")
    Page<Product> searchForAdmin(
            @Param("keyword") String keyword,
            @Param("status") ProductStatus status,
            @Param("shopId") Long shopId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p " +
            "WHERE p.status = :productStatus " +
            "AND p.shop.status = :shopStatus " +
            "AND p.category.categoryStatus = :categoryStatus " +
            "ORDER BY p.productId DESC")
    List<Product> findTop10SellableProducts(
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("categoryStatus") CategoryStatus categoryStatus,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p " +
            "WHERE p.status = :productStatus " +
            "AND p.shop.status = :shopStatus " +
            "AND p.category.categoryStatus = :categoryStatus " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
            "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)")
    Page<Product> searchActiveProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("categoryStatus") CategoryStatus categoryStatus,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p.brand FROM Product p " +
            "WHERE p.brand IS NOT NULL " +
            "AND TRIM(p.brand) <> '' " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.brand ASC")
    List<String> searchDistinctBrands(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    Long countByShop_ShopIdAndStatus(
            Long shopId,
            ProductStatus status
    );

    Page<Product> findByShop_ShopIdAndStatusOrderByProductIdDesc(
            Long shopId,
            ProductStatus status,
            Pageable pageable
    );
}