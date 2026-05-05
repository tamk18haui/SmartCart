package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Lệnh mới: Lấy sản phẩm của Shop VÀ bỏ qua các sản phẩm đã bị xóa (DELETED), có kèm phân trang
    Page<Product> findByShopShopIdAndStatusNot(Long shopId, ProductStatus status, Pageable pageable);
}