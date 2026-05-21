package com.gr6.SmartCart.modules.finance_core.service.impl;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.domain.UserVoucherUsage;
import com.gr6.SmartCart.common.domain.Voucher;
import com.gr6.SmartCart.common.enums.DiscountType;
import com.gr6.SmartCart.common.enums.VoucherStatus;
import com.gr6.SmartCart.modules.finance_core.dto.ShopVoucherResponse;
import com.gr6.SmartCart.modules.finance_core.repository.UserVoucherUsageRepository;
import com.gr6.SmartCart.modules.finance_core.repository.VoucherRepository;
import com.gr6.SmartCart.modules.finance_core.service.VoucherService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherUsageRepository usageRepository;
    private final UserRepository userRepository;
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private LocalDateTime nowVn() {
        return LocalDateTime.now(VN_ZONE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopVoucherResponse> getShopVouchers(
            Long shopId,
            Long userId,
            Long orderValue
    ) {
        if (shopId == null || shopId <= 0) {
            throw new RuntimeException("Shop không hợp lệ");
        }

        return voucherRepository.findBuyerVouchersByShopId(shopId)
                .stream()
                .map(voucher -> {
                    boolean usedByCurrentUser = isUsedByCurrentUser(voucher, userId);
                    String unavailableReason = getUnavailableReason(
                            voucher,
                            shopId,
                            userId,
                            orderValue,
                            false
                    );

                    boolean usable = unavailableReason == null;

                    return ShopVoucherResponse.fromEntity(
                            voucher,
                            usable,
                            usedByCurrentUser,
                            unavailableReason
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long calculateDiscount(
            String code,
            Long totalAmount,
            Long shopId,
            Long userId
    ) {
        if (isBlank(code)) {
            return 0L;
        }

        if (totalAmount == null || totalAmount <= 0) {
            return 0L;
        }

        Voucher voucher = voucherRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));

        String error = getUnavailableReason(
                voucher,
                shopId,
                userId,
                totalAmount,
                true
        );

        if (error != null) {
            throw new RuntimeException(error);
        }

        return calculateDiscountAmount(voucher, totalAmount);
    }

    @Override
    @Transactional
    public void useVoucher(
            String code,
            Long userId
    ) {
        if (isBlank(code)) {
            return;
        }

        if (userId == null || userId <= 0) {
            throw new RuntimeException("Người dùng không hợp lệ");
        }

        Voucher voucher = voucherRepository.findByCodeWithLock(normalizeCode(code))
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));

        String error = getUnavailableReason(
                voucher,
                voucher.getShop() == null ? null : voucher.getShop().getShopId(),
                userId,
                null,
                false
        );

        if (error != null) {
            throw new RuntimeException(error);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserVoucherUsage usage = usageRepository
                .findByUser_UserIdAndVoucher_VoucherId(userId, voucher.getVoucherId())
                .orElseGet(() -> {
                    UserVoucherUsage newUsage = new UserVoucherUsage();
                    newUsage.setUser(user);
                    newUsage.setVoucher(voucher);
                    newUsage.setUsedCount(0);
                    return newUsage;
                });

        if (safeInt(usage.getUsedCount()) >= 1) {
            throw new RuntimeException("Bạn đã sử dụng mã giảm giá này rồi");
        }

        voucher.setUsedCount(safeInt(voucher.getUsedCount()) + 1);
        usage.setUsedCount(safeInt(usage.getUsedCount()) + 1);

        voucherRepository.save(voucher);
        usageRepository.save(usage);
    }

    private String getUnavailableReason(
            Voucher voucher,
            Long shopId,
            Long userId,
            Long orderValue,
            boolean requireOrderValue
    ) {
        if (voucher == null) {
            return "Mã giảm giá không tồn tại";
        }

        if (voucher.getStatus() == null || voucher.getStatus() != VoucherStatus.ACTIVE) {
            return "Mã giảm giá không còn hoạt động";
        }

        LocalDateTime now = nowVn();

        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            return "Mã giảm giá chưa bắt đầu";
        }

        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            return "Mã giảm giá đã hết hạn";
        }

        if (voucher.getShop() == null || voucher.getShop().getShopId() == null) {
            return "Mã giảm giá không thuộc shop hợp lệ";
        }

        if (shopId != null && !voucher.getShop().getShopId().equals(shopId)) {
            return "Mã giảm giá không thuộc shop này";
        }

        int usageLimit = safeInt(voucher.getUsageLimit());
        int usedCount = safeInt(voucher.getUsedCount());

        if (usageLimit > 0 && usedCount >= usageLimit) {
            return "Mã giảm giá đã hết lượt sử dụng";
        }

        if (userId != null && userId > 0 && isUsedByCurrentUser(voucher, userId)) {
            return "Bạn đã sử dụng mã giảm giá này rồi";
        }

        if (requireOrderValue && (orderValue == null || orderValue <= 0)) {
            return "Giá trị đơn hàng không hợp lệ";
        }

        long minOrderValue = safeLong(voucher.getMinOrderValue());

        if (orderValue != null && minOrderValue > 0 && orderValue < minOrderValue) {
            long missing = minOrderValue - orderValue;
            return "Cần mua thêm " + formatVnd(missing) + " để dùng mã này";
        }

        return null;
    }

    private boolean isUsedByCurrentUser(Voucher voucher, Long userId) {
        if (voucher == null || voucher.getVoucherId() == null) {
            return false;
        }

        if (userId == null || userId <= 0) {
            return false;
        }

        return usageRepository
                .findByUser_UserIdAndVoucher_VoucherId(userId, voucher.getVoucherId())
                .map(usage -> safeInt(usage.getUsedCount()) >= 1)
                .orElse(false);
    }

    private Long calculateDiscountAmount(Voucher voucher, Long totalAmount) {
        if (voucher == null || totalAmount == null || totalAmount <= 0) {
            return 0L;
        }

        long discountValue = safeLong(voucher.getDiscountValue());
        long discount;

        if (voucher.getDiscountType() == DiscountType.PERCENT) {
            discount = totalAmount * discountValue / 100L;
        } else {
            discount = discountValue;
        }

        long maxDiscount = safeLong(voucher.getMaxDiscountAmount());

        if (maxDiscount > 0) {
            discount = Math.min(discount, maxDiscount);
        }

        discount = Math.max(0L, discount);
        discount = Math.min(discount, totalAmount);

        return discount;
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private String formatVnd(Long value) {
        long amount = value == null ? 0L : value;
        return String.format("%,d", amount).replace(",", ".") + "đ";
    }
}