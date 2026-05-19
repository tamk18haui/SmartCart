package com.gr6.SmartCart.module_v3.withdraw.dto;

import com.gr6.SmartCart.common.domain.WithdrawRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WithdrawResponse {
    private Long withdrawId;
    private Long walletId;
    private Long sellerId;
    private String sellerEmail;
    private String sellerName;
    private Long shopId;
    private String shopName;

    private Long amount;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;
    private String sellerNote;

    private String status;
    private String adminNote;
    private String transferCode;
    private String processedBy;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WithdrawResponse from(WithdrawRequest request) {
        return WithdrawResponse.builder()
                .withdrawId(request.getWithdrawId())
                .walletId(request.getWallet().getWalletId())
                .sellerId(request.getSeller().getUserId())
                .sellerEmail(request.getSeller().getEmail())
                .sellerName(request.getSeller().getFullName())
                .shopId(request.getShop().getShopId())
                .shopName(request.getShop().getShopName())
                .amount(request.getAmount())
                .bankName(request.getBankName())
                .bankAccountNumber(mask(request.getBankAccountNumber()))
                .bankAccountHolder(request.getBankAccountHolder())
                .sellerNote(request.getSellerNote())
                .status(request.getStatus().name())
                .adminNote(request.getAdminNote())
                .transferCode(request.getTransferCode())
                .processedBy(request.getProcessedBy())
                .processedAt(request.getProcessedAt())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    private static String mask(String value) {
        if (value == null || value.length() <= 4) return value;
        return "****" + value.substring(value.length() - 4);
    }
}