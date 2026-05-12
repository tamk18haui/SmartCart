package com.gr6.SmartCart.module_v2.promotion.dto;


import com.gr6.SmartCart.common.enums.DiscountType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class VoucherResponse {
    private Long voucherId;
    private String code;
    private DiscountType discountType;
    private Long discountValue;
    private Long minOrderValue;
    private Long maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
}