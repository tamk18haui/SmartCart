package com.gr6.SmartCart.modules.finance_core.service.impl;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.domain.UserVoucherUsage;
import com.gr6.SmartCart.common.domain.Voucher;
import com.gr6.SmartCart.common.enums.DiscountType;
import com.gr6.SmartCart.modules.finance_core.repository.UserVoucherUsageRepository;
import com.gr6.SmartCart.modules.finance_core.repository.VoucherRepository;
import com.gr6.SmartCart.modules.finance_core.service.VoucherService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final VoucherRepository voucherRepository;
    private final UserVoucherUsageRepository userVoucherUsageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Long calculateDiscount(String code, Long totalAmount, Long shopId, Long userId) {
        if (isBlank(code)) {
            return 0L;
        }

        if (totalAmount == null || totalAmount <= 0) {
            return 0L;
        }

        Voucher voucher = voucherRepository.findByCode(code.trim())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

        validateVoucher(voucher, totalAmount, shopId, userId);

        return calculateAmount(voucher, totalAmount);
    }

    @Override
    @Transactional
    public void useVoucher(String code, Long userId) {
        if (isBlank(code)) {
            return;
        }

        Voucher voucher = voucherRepository.findByCodeWithLock(code.trim())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

        validateVoucher(voucher, null, null, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        UserVoucherUsage usage = userVoucherUsageRepository
                .findByUser_UserIdAndVoucher_VoucherId(userId, voucher.getVoucherId())
                .orElseGet(() -> {
                    UserVoucherUsage newUsage = new UserVoucherUsage();
                    newUsage.setUser(user);
                    newUsage.setVoucher(voucher);
                    newUsage.setUsedCount(0);
                    return newUsage;
                });

        if (usage.getUsedCount() != null && usage.getUsedCount() >= 1) {
            throw new RuntimeException("Bạn đã sử dụng mã giảm giá này rồi!");
        }

        voucher.setUsedCount(safeInt(voucher.getUsedCount()) + 1);
        usage.setUsedCount(safeInt(usage.getUsedCount()) + 1);

        voucherRepository.save(voucher);
        userVoucherUsageRepository.save(usage);
    }

    private void validateVoucher(Voucher voucher, Long totalAmount, Long shopId, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        if (!ACTIVE_STATUS.equalsIgnoreCase(voucher.getStatus())) {
            throw new RuntimeException("Mã giảm giá không còn hoạt động!");
        }

        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            throw new RuntimeException("Mã giảm giá chưa đến thời gian sử dụng!");
        }

        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn!");
        }

        if (voucher.getUsageLimit() != null
                && voucher.getUsageLimit() > 0
                && safeInt(voucher.getUsedCount()) >= voucher.getUsageLimit()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
        }

        if (shopId != null
                && voucher.getShop() != null
                && !voucher.getShop().getShopId().equals(shopId)) {
            throw new RuntimeException("Mã giảm giá không thuộc shop này!");
        }

        if (totalAmount != null
                && voucher.getMinOrderValue() != null
                && totalAmount < voucher.getMinOrderValue()) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để dùng mã!");
        }

        if (userId != null) {
            userVoucherUsageRepository
                    .findByUser_UserIdAndVoucher_VoucherId(userId, voucher.getVoucherId())
                    .ifPresent(usage -> {
                        if (safeInt(usage.getUsedCount()) >= 1) {
                            throw new RuntimeException("Bạn đã sử dụng mã giảm giá này rồi!");
                        }
                    });
        }
    }

    private Long calculateAmount(Voucher voucher, Long totalAmount) {
        long discount;

        if (voucher.getDiscountType() == DiscountType.PERCENT) {
            discount = totalAmount * voucher.getDiscountValue() / 100;
        } else {
            discount = voucher.getDiscountValue();
        }

        if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount() > 0) {
            discount = Math.min(discount, voucher.getMaxDiscountAmount());
        }

        return Math.max(0L, Math.min(discount, totalAmount));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}