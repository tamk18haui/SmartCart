package com.gr6.SmartCart.modules.finance_core.repository;

import com.gr6.SmartCart.common.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
