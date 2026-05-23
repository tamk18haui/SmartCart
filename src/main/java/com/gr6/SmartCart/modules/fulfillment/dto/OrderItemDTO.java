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
    private String variantName;
    private int quantity;
    private BigDecimal price;

    // Ảnh sản phẩm/biến thể để Android seller order load lên
    private String imageUrl;
}