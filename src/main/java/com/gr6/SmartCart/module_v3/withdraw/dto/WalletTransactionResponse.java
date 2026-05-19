package com.gr6.SmartCart.module_v3.withdraw.dto;

import com.gr6.SmartCart.common.domain.WalletTransaction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WalletTransactionResponse {
    private Long walletTxId;
    private String type;
    private Long amount;
    private String description;
    private LocalDateTime createdAt;

    public static WalletTransactionResponse from(WalletTransaction tx) {
        return WalletTransactionResponse.builder()
                .walletTxId(tx.getWalletTxId())
                .type(tx.getType() == null ? null : tx.getType().name())
                .amount(tx.getAmount())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}