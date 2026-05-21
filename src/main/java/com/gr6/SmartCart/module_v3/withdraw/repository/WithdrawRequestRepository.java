package com.gr6.SmartCart.module_v3.withdraw.repository;

import com.gr6.SmartCart.common.domain.WithdrawRequest;
import com.gr6.SmartCart.common.enums.WithdrawStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WithdrawRequestRepository extends JpaRepository<WithdrawRequest, Long> {

    Page<WithdrawRequest> findBySeller_EmailOrderByWithdrawIdDesc(
            String email,
            Pageable pageable
    );

    Page<WithdrawRequest> findByStatusOrderByWithdrawIdDesc(
            WithdrawStatus status,
            Pageable pageable
    );

    Page<WithdrawRequest> findAllByOrderByWithdrawIdDesc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WithdrawRequest w WHERE w.withdrawId = :withdrawId")
    Optional<WithdrawRequest> findByIdForUpdate(@Param("withdrawId") Long withdrawId);
}