package com.gr6.SmartCart.modules.fulfillment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReviewCreateRequest {

    @NotNull(message = "Vui lòng chọn sản phẩm cần đánh giá")
    private Long orderItemId;

    @NotNull(message = "Vui lòng chọn số sao đánh giá")
    @Min(value = 1, message = "Số sao đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Số sao đánh giá tối đa là 5")
    private Integer rating;

    @Size(max = 2000, message = "Nội dung nhận xét không được vượt quá 2000 ký tự")
    private String comment;

    // Tối đa 4 ảnh
    private List<String> imageUrls;

    // Tối đa 1 video
    private String videoUrl;
}