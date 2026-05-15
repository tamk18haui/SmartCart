package com.gr6.SmartCart.modules.storefront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopPublicResponse {

    private Long shopId;
    private String shopName;
    private String description;
    private String pickupAddress;
    private String status;

    private Long productCount;
    private Long voucherCount;

    private Double ratingAverage;
    private Long reviewCount;
}