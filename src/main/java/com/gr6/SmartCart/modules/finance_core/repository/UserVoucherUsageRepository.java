package com.gr6.SmartCart.modules.finance_core.repository;

import com.gr6.SmartCart.common.domain.UserVoucherUsage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVoucherUsageRepository extends JpaRepository<UserVoucherUsage, Long> {

    Optional<UserVoucherUsage> findByUser_UserIdAndVoucher_VoucherId(
            Long userId,
            Long voucherId
    );
}