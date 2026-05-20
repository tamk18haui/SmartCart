package com.gr6.SmartCart.module_v3.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionStatResponse {
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private Long totalVolume; // Tổng tiền giao dịch thành công
}