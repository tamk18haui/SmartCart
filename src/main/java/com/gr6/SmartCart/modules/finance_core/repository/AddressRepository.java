package com.gr6.SmartCart.modules.finance_core.repository;

import com.gr6.SmartCart.common.domain.Address;
import com.gr6.SmartCart.common.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
}
