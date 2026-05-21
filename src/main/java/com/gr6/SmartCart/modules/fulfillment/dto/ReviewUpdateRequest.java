package com.gr6.SmartCart.modules.fulfillment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewUpdateRequest {

    @NotNull(message = "Vui lòng chọn số sao đánh giá")
    @Min(value = 1, message = "Số sao đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Số sao đánh giá tối đa là 5")
    private Integer rating;

    @Size(max = 2000, message = "Nội dung nhận xét không được vượt quá 2000 ký tự")
    private String comment;

    private String imageUrl;
}