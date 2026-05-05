package com.gr6.SmartCart.modules.identity.repository;

import com.gr6.SmartCart.common.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByShopName(String shopName);
}