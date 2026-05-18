package com.gr6.SmartCart.modules.storefront.repository;

import com.gr6.SmartCart.common.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopPublicRepository extends JpaRepository<Shop, Long> {
}