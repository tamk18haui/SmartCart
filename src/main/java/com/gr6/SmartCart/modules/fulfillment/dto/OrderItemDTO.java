package com.gr6.SmartCart.modules.fulfillment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private String productName;
    private String variantName; // Phân loại (vd: Màu đỏ, XL)
    private int quantity;
    private BigDecimal price;
}