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

    // Lấy danh sách voucher của 1 shop
    List<Voucher> findByShop_ShopId(Long shopId);

    // Tìm voucher cụ thể của 1 shop
    Optional<Voucher> findByVoucherIdAndShop_ShopId(Long voucherId, Long shopId);

    // Kiểm tra mã voucher trong 1 shop
    boolean existsByShop_ShopIdAndCode(Long shopId, String code);

    // Kiểm tra mã voucher không phân biệt hoa thường trong 1 shop
    boolean existsByShop_ShopIdAndCodeIgnoreCase(Long shopId, String code);

    // Kiểm tra mã voucher toàn hệ thống
    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            SELECT v
            FROM Voucher v
            LEFT JOIN FETCH v.shop s
            WHERE UPPER(v.code) = UPPER(:code)
            """)
    Optional<Voucher> findByCode(@Param("code") String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT v
            FROM Voucher v
            LEFT JOIN FETCH v.shop s
            WHERE UPPER(v.code) = UPPER(:code)
            """)
    Optional<Voucher> findByCodeWithLock(@Param("code") String code);

    @Query("""
            SELECT v
            FROM Voucher v
            LEFT JOIN FETCH v.shop s
            WHERE s.shopId = :shopId
            ORDER BY 
                CASE 
                    WHEN v.discountType = com.gr6.SmartCart.common.enums.DiscountType.PERCENT THEN 1
                    ELSE 2
                END,
                v.discountValue DESC
            """)
    List<Voucher> findBuyerVouchersByShopId(@Param("shopId") Long shopId);
}