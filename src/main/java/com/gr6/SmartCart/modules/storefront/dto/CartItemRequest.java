package com.gr6.SmartCart.modules.storefront.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull(message = "Thiếu thông tin phân loại sản phẩm")
    private Long variantId;

    @NotNull(message = "Thiếu số lượng")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
}