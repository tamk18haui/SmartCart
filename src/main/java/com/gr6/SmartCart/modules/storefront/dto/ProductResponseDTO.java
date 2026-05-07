package com.gr6.SmartCart.modules.storefront.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductResponseDTO {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    
    // --- CÁC TRƯỜNG BỔ SUNG ---
    private Integer soldQuantity;   // Số lượng đã bán [cite: 66]
    private Double averageRating;    // Số sao trung bình 
    private String location;         // Địa chỉ shop (Tỉnh/Thành phố) [cite: 98]
}