package com.gr6.SmartCart.modules.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductVariantRequest {

    /*
     * Không bắt buộc app nhập.
     * Nếu null, backend tự sinh SKU theo productId.
     */
    private String sku;

    /*
     * Giá riêng của biến thể.
     * Nếu null, backend lấy basePrice của sản phẩm.
     */
    @DecimalMin(value = "0.0", message = "Giá biến thể không được âm")
    private BigDecimal price;

    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stockQuantity;

    /*
     * Ảnh riêng của biến thể.
     * Android upload Cloudinary trước rồi gửi URL về backend.
     */
    private String imageUrl;

    /*
     * Ví dụ:
     * {
     *   "Màu sắc": "Đen",
     *   "Kích cỡ": "M"
     * }
     */
    private Map<String, String> attributes;
}