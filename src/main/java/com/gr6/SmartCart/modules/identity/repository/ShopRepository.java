package com.gr6.SmartCart.modules.identity.repository;

import com.gr6.SmartCart.common.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByShopName(String shopName);
    Optional<Shop> findByUser_UserId(Long userId);
    Optional<Shop> findByUser_Email(String email);

}