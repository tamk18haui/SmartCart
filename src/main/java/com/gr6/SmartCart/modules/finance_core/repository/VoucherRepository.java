package com.gr6.SmartCart.modules.finance_core.repository;

import com.gr6.SmartCart.common.domain.Voucher;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    // Hàm 1: Đọc thông thường (Dùng cho API Preview - Chống Deadlock)
    Optional<Voucher> findByCode(String code);

    // Hàm 2: Đọc và Khóa (Dùng cho API Checkout - Chống Race Condition)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Voucher v WHERE v.code = :code")

    Optional<Voucher> findByCodeWithLock(@Param("code") String code);
    // Lấy danh sách Voucher của 1 Shop
    List<Voucher> findByShop_ShopId(Long shopId);


    // Tìm chi tiết 1 Voucher cụ thể của 1 Shop
    Optional<Voucher> findByVoucherIdAndShop_ShopId(Long voucherId, Long shopId);

    // Kiểm tra xem mã Code đã tồn tại trong Shop chưa (dùng khi tạo mới)
    boolean existsByShop_ShopIdAndCode(Long shopId, String code);
}