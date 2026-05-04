package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Tìm tất cả sản phẩm của một Shop cụ thể
    List<Product> findByShopShopId(Long shopId);
}