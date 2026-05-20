package com.gr6.SmartCart.module_v3.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {
    private Long productId;
    private String name;
    private String imageUrl; // Lấy ảnh đầu tiên của sản phẩm
    private Long basePrice;
    private Long totalSold;
    private Long totalRevenue;
}