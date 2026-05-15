package com.gr6.SmartCart.modules.catalog.dto;

import com.gr6.SmartCart.common.enums.ProductCondition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;

    /*
     * Brand không fix cứng.
     * App có thể gọi API gợi ý brand.
     * Nếu không có brand trong danh sách, seller tự nhập text mới.
     */
    private String brand;

    @NotNull(message = "Tình trạng sản phẩm không được để trống")
    private ProductCondition condition;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "1.0", message = "Giá phải lớn hơn 0")
    private BigDecimal basePrice;

    @NotNull(message = "Cân nặng không được để trống")
    @DecimalMin(value = "0.01", message = "Cân nặng phải lớn hơn 0")
    private BigDecimal weight;

    @DecimalMin(value = "0.01", message = "Chiều dài phải lớn hơn 0")
    private BigDecimal length;

    @DecimalMin(value = "0.01", message = "Chiều rộng phải lớn hơn 0")
    private BigDecimal width;

    @DecimalMin(value = "0.01", message = "Chiều cao phải lớn hơn 0")
    private BigDecimal height;

    /*
     * Nếu không có phân loại hàng, backend dùng stockQuantity để tạo biến thể mặc định.
     * Nếu có variants, backend lấy tồn kho theo từng biến thể.
     */
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stockQuantity;

    /*
     * Android upload ảnh trực tiếp lên Cloudinary.
     * Backend chỉ nhận URL và lưu vào DB.
     */
    private List<String> uploadImages;

    /*
     * Danh sách biến thể sản phẩm.
     * Ví dụ: màu sắc, kích cỡ, giá riêng, tồn kho riêng, ảnh riêng.
     */
    @Valid
    private List<ProductVariantRequest> variants;
}