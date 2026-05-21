package com.gr6.SmartCart.module_v3.withdraw.dto;

import com.gr6.SmartCart.common.domain.SellerSettlement;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SellerSettlementResponse {
    private Long settlementId;
    private Long shopOrderId;
    private Long orderId;
    private Long sellerId;
    private String sellerEmail;
    private Long shopId;
    private String shopName;
    private Long grossAmount;
    private Long commissionAmount;
    private Long netAmount;
    private String status;
    private String note;
    private String settledBy;
    private LocalDateTime createdAt;

    public static SellerSettlementResponse from(SellerSettlement settlement) {
        return SellerSettlementResponse.builder()
                .settlementId(settlement.getSettlementId())
                .shopOrderId(settlement.getShopOrder().getShopOrderId())
                .orderId(settlement.getShopOrder().getOrder().getOrderId())
                .sellerId(settlement.getSeller().getUserId())
                .sellerEmail(settlement.getSeller().getEmail())
                .shopId(settlement.getShop().getShopId())
                .shopName(settlement.getShop().getShopName())
                .grossAmount(settlement.getGrossAmount())
                .commissionAmount(settlement.getCommissionAmount())
                .netAmount(settlement.getNetAmount())
                .status(settlement.getStatus().name())
                .note(settlement.getNote())
                .settledBy(settlement.getSettledBy())
                .createdAt(settlement.getCreatedAt())
                .build();
    }
}