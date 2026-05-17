package com.gr6.SmartCart.modules.finance_core.dto;

import com.gr6.SmartCart.common.domain.Voucher;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopVoucherResponse {

    private Long voucherId;
    private Long shopId;

    private String code;
    private String discountType;

    private Long discountValue;
    private Long minOrderValue;
    private Long maxDiscountAmount;

    private Integer usageLimit;
    private Integer usedCount;

    private String startDate;
    private String endDate;

    private String status;

    private Boolean usable;
    private Boolean usedByCurrentUser;
    private String unavailableReason;

    private String displayTitle;
    private String displaySubtitle;

    public static ShopVoucherResponse fromEntity(
            Voucher voucher,
            Boolean usable,
            Boolean usedByCurrentUser,
            String unavailableReason
    ) {
        String discountType = voucher.getDiscountType() == null
                ? null
                : voucher.getDiscountType().name();

        Long discountValue = voucher.getDiscountValue() == null
                ? 0L
                : voucher.getDiscountValue();

        Long minOrderValue = voucher.getMinOrderValue() == null
                ? 0L
                : voucher.getMinOrderValue();

        Long maxDiscountAmount = voucher.getMaxDiscountAmount();

        String title;

        if ("PERCENT".equalsIgnoreCase(discountType)) {
            title = "Giảm " + discountValue + "%";
        } else {
            title = "Giảm " + formatVnd(discountValue);
        }

        StringBuilder subtitle = new StringBuilder();

        if (minOrderValue > 0) {
            subtitle.append("Đơn tối thiểu ").append(formatVnd(minOrderValue));
        } else {
            subtitle.append("Không yêu cầu đơn tối thiểu");
        }

        if (maxDiscountAmount != null && maxDiscountAmount > 0) {
            subtitle.append(" • Tối đa ").append(formatVnd(maxDiscountAmount));
        }

        return ShopVoucherResponse.builder()
                .voucherId(voucher.getVoucherId())
                .shopId(voucher.getShop() != null ? voucher.getShop().getShopId() : null)
                .code(voucher.getCode())
                .discountType(discountType)
                .discountValue(discountValue)
                .minOrderValue(minOrderValue)
                .maxDiscountAmount(maxDiscountAmount)
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount() == null ? 0 : voucher.getUsedCount())
                .startDate(voucher.getStartDate() == null ? null : voucher.getStartDate().toString())
                .endDate(voucher.getEndDate() == null ? null : voucher.getEndDate().toString())
                .status(voucher.getStatus() == null ? null : voucher.getStatus().name())
                .usable(usable)
                .usedByCurrentUser(usedByCurrentUser)
                .unavailableReason(unavailableReason)
                .displayTitle(title)
                .displaySubtitle(subtitle.toString())
                .build();
    }

    private static String formatVnd(Long amount) {
        if (amount == null) return "0đ";
        return String.format("%,d", amount).replace(",", ".") + "đ";
    }
}