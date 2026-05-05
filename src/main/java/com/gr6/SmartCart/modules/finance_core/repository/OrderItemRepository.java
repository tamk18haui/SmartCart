package com.gr6.SmartCart.modules.finance_core.repository;

import com.gr6.SmartCart.common.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
