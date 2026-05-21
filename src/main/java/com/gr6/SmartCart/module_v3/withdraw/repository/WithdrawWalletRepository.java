package com.gr6.SmartCart.module_v3.withdraw.repository;

import com.gr6.SmartCart.common.domain.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WithdrawWalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser_UserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.userId = :userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") Long userId);
}