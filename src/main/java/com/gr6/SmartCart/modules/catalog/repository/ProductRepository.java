package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByShopShopIdAndStatusNot(Long shopId, ProductStatus status, Pageable pageable);

    // SÁNG THÊM: Lọc sản phẩm cho Admin
    @Query("SELECT p FROM Product p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchForAdmin(@Param("status") ProductStatus status, @Param("keyword") String keyword, Pageable pageable);
}