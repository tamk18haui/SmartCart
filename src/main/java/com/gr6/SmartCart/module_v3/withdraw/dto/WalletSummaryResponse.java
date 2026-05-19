package com.gr6.SmartCart.module_v3.withdraw.dto;

import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.Wallet;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WalletSummaryResponse {
    private Long walletId;
    private Long sellerId;
    private Long shopId;
    private String shopName;
    private Long balance;
    private String status;
    private LocalDateTime updatedAt;

    public static WalletSummaryResponse from(Wallet wallet, Shop shop) {
        return WalletSummaryResponse.builder()
                .walletId(wallet.getWalletId())
                .sellerId(wallet.getUser().getUserId())
                .shopId(shop.getShopId())
                .shopName(shop.getShopName())
                .balance(wallet.getBalance() == null ? 0L : wallet.getBalance())
                .status(wallet.getStatus() == null ? null : wallet.getStatus().name())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}