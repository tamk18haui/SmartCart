package com.gr6.SmartCart.modules.identity.repository;

import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.enums.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    boolean existsByShopName(String shopName);

    // TRẢ LẠI DẤU GẠCH DƯỚI ĐỂ KHÔNG BỊ LỖI Ở SHOP MANAGER
    Optional<Shop> findByUser_UserId(Long userId);

    List<Shop> findAllByUser_UserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT s FROM Shop s " +
            "WHERE (:status IS NULL OR s.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "OR LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.pickupAddress) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY s.shopId DESC")
    Page<Shop> searchForAdmin(
            @Param("status") ShopStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}