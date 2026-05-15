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

    public static ShopVoucherResponse fromEntity(Voucher voucher) {
        return ShopVoucherResponse.builder()
                .voucherId(voucher.getVoucherId())
                .shopId(voucher.getShop() != null ? voucher.getShop().getShopId() : null)
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType() != null ? voucher.getDiscountType().name() : null)
                .discountValue(voucher.getDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startDate(voucher.getStartDate() != null ? voucher.getStartDate().toString() : null)
                .endDate(voucher.getEndDate() != null ? voucher.getEndDate().toString() : null)
                .status(voucher.getStatus() != null ? voucher.getStatus().name() : null)
                .build();
    }
}