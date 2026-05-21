package com.gr6.SmartCart.module_v3.withdraw.repository;

import com.gr6.SmartCart.common.domain.WalletTransaction;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawWalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWallet_WalletIdOrderByWalletTxIdDesc(
            Long walletId,
            Pageable pageable
    );
}