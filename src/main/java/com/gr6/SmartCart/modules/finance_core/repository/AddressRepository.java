package com.gr6.SmartCart.modules.finance_core.repository;

import com.gr6.SmartCart.common.domain.Address;
import com.gr6.SmartCart.common.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser(User user);

    List<Address> findByUserAndIsDeletedFalse(User user);

    Optional<Address> findByAddressIdAndUserAndIsDeletedFalse(Long addressId, User user);
    Optional<Address> findByUserAndIsDefaultTrueAndIsDeletedFalse(User user);

    Optional<Address> findFirstByUserAndIsDeletedFalseOrderByAddressIdAsc(User user);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user AND a.isDeleted = false AND a.isDefault = true")
    void clearDefaultAddress(User user);
}