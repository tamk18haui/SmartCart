package com.gr6.SmartCart.module_v2.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressRequestDTO {
    @NotBlank(message = "Tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ (Phải là số ĐT Việt Nam)")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String fullAddress;

    private Boolean isDefault;
}