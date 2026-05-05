package com.gr6.SmartCart.modules.finance_core.service.impl;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.domain.UserVoucherUsage;
import com.gr6.SmartCart.common.domain.Voucher;
import com.gr6.SmartCart.common.enums.DiscountType;
import com.gr6.SmartCart.modules.finance_core.repository.UserVoucherUsageRepository;
import com.gr6.SmartCart.modules.finance_core.repository.VoucherRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.finance_core.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherUsageRepository usageRepository;
    private final UserRepository userRepository;

    @Override
    public Long calculateDiscount(String code, Long totalAmount, Long shopId, Long userId) {
        if (code == null || code.isBlank()) return 0L;

        // Dùng hàm KHÔNG LOCK để tránh kẹt hệ thống
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        validateVoucher(voucher, shopId, totalAmount);

        // KIỂM TRA LƯỢT DÙNG CỦA CÁ NHÂN (Mỗi người 1 lần)
        usageRepository.findByUser_UserIdAndVoucher_VoucherId(userId, voucher.getVoucherId())
                .ifPresent(u -> {
                    if (u.getUsedCount() >= 1) throw new RuntimeException("Bạn đã sử dụng mã giảm giá này rồi!");
                });

        return calculate(voucher, totalAmount);
    }

    @Override
    @Transactional
    public void useVoucher(String code, Long userId) {
        if (code == null || code.isBlank()) return;

        // Dùng hàm CÓ LOCK để trừ lượt an toàn
        Voucher voucher = voucherRepository.findByCodeWithLock(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // Trừ lượt chung
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);

        // Ghi nhận lượt dùng của User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        UserVoucherUsage usage = usageRepository.findByUser_UserIdAndVoucher_VoucherId(userId, voucher.getVoucherId())
                .orElse(new UserVoucherUsage());

        usage.setUser(user);
        usage.setVoucher(voucher);
        usage.setUsedCount(usage.getUsedCount() != null ? usage.getUsedCount() + 1 : 1);
        usageRepository.save(usage);
    }

    // Hàm phụ trợ gom logic kiểm tra
    private void validateVoucher(Voucher voucher, Long shopId, Long totalAmount) {
        if (!"ACTIVE".equalsIgnoreCase(voucher.getStatus())) throw new RuntimeException("Voucher không khả dụng");
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) throw new RuntimeException("Voucher chưa bắt đầu");
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) throw new RuntimeException("Voucher đã hết hạn");
        if (!voucher.getShop().getShopId().equals(shopId)) throw new RuntimeException("Voucher không thuộc shop này");
        if (voucher.getMinOrderValue() != null && totalAmount < voucher.getMinOrderValue()) throw new RuntimeException("Chưa đủ giá trị đơn");
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) throw new RuntimeException("Voucher đã hết lượt trên toàn hệ thống");
    }

    private long calculate(Voucher v, Long total) {
        long discount = (v.getDiscountType() == DiscountType.FIXED) ? v.getDiscountValue() : (total * v.getDiscountValue()) / 100;
        if (v.getMaxDiscountAmount() != null) discount = Math.min(discount, v.getMaxDiscountAmount());
        return Math.min(discount, total);
    }
}