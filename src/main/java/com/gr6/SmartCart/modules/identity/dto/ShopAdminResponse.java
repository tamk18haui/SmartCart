package com.gr6.SmartCart.modules.identity.dto;

import com.gr6.SmartCart.common.domain.Shop;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopAdminResponse {
    private Long shopId;
    private String shopName;
    private String ownerEmail;
    private String pickupAddress;
    private String status;

    public static ShopAdminResponse fromEntity(Shop shop) {
        return ShopAdminResponse.builder()
                .shopId(shop.getShopId())
                .shopName(shop.getShopName())
                .ownerEmail(shop.getUser().getEmail())
                .pickupAddress(shop.getPickupAddress())
                .status(shop.getStatus().name())
                .build();
    }
}