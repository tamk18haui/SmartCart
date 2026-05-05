package com.gr6.SmartCart.modules.finance_core.repository;

import com.gr6.SmartCart.common.domain.Voucher;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    // Hàm 1: Đọc thông thường (Dùng cho API Preview - Chống Deadlock)
    Optional<Voucher> findByCode(String code);

    // Hàm 2: Đọc và Khóa (Dùng cho API Checkout - Chống Race Condition)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Voucher v WHERE v.code = :code")
    Optional<Voucher> findByCodeWithLock(@Param("code") String code);
}