package com.gr6.SmartCart.modules.identity.repository;

import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.enums.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByShopName(String shopName);

    // SÁNG THÊM: Lọc Shop cho Admin
    @Query("SELECT s FROM Shop s WHERE " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Shop> searchForAdmin(@Param("status") ShopStatus status, @Param("keyword") String keyword, Pageable pageable);
}