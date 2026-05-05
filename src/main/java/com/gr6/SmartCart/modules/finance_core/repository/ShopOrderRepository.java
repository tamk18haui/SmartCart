package com.gr6.SmartCart.modules.finance_core.repository;

import com.gr6.SmartCart.common.domain.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {
}
