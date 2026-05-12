package com.gr6.SmartCart.module_v2.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponseDTO {
    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String fullAddress;
    private Boolean isDefault;
}