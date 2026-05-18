package com.gr6.SmartCart.modules.storefront.dto;

import lombok.Data;

@Data
public class CartItemResponseDTO {
    private Long cartItemId;
    private Long variantId;
    private Long productId;
    private String variantSku;
    
    private String productName;
    private String variantAttributes; // Chứa chuỗi VD: "Da dầu 236ml" hoặc "Màu đỏ, Size L"
    private String imageUrl;          // Ảnh riêng của biến thể
    
    private Double price;
    private Integer quantity;
    private Integer maxQuantity;      // Trả về tồn kho để Frontend chặn nút [+]
}