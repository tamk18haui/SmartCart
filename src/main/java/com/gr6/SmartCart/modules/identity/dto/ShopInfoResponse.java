package com.gr6.SmartCart.modules.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopInfoResponse {
    private Long shopId;
    private String shopName;
    private String pickupAddress;
    private String description;
    private String status;
}