package com.gr6.SmartCart.modules.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ShopManagerRequest {
    @NotBlank(message = "Tên cửa hàng không được để trống!")
    private String shopName;

    @NotBlank(message = "Địa chỉ lấy hàng không được để trống!")
    private String pickupAddress;

    private String description;
}