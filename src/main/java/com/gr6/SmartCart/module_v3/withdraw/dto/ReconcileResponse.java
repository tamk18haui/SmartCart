package com.gr6.SmartCart.module_v3.withdraw.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReconcileResponse {
    private Integer settledCount;
    private Long totalGrossAmount;
    private Long totalCommissionAmount;
    private Long totalNetAmount;
    private List<SellerSettlementResponse> settlements;
}