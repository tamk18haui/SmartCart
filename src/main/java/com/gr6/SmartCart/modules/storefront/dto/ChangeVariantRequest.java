package com.gr6.SmartCart.modules.storefront.dto;

import lombok.Data;

@Data
public class ChangeVariantRequest {
    private Long cartItemId;   // ID của dòng sản phẩm trong giỏ cần đổi
    private Long newVariantId; // ID của biến thể mới được chọn
}