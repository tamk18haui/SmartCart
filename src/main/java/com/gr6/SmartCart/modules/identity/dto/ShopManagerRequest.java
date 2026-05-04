
package com.gr6.SmartCart.modules.identity.dto;

import lombok.Data;

@Data
public class ShopManagerRequest {
    private String shopName;
    private String description;
    private String pickupAddress;
    private String avatarUrl;      // Để cập nhật logo shop[cite: 1]
}