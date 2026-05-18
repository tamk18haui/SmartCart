package com.gr6.SmartCart.module_v2.promotion.dto;


import com.gr6.SmartCart.common.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoucherRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Pattern(regexp = "^[A-Z0-9]{5,20}$", message = "Mã code viết hoa, không dấu, không khoảng trắng (5-20 ký tự)")
    private String code;

    @NotNull(message = "Loại giảm giá không hợp lệ")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @Min(value = 1, message = "Giá trị giảm phải lớn hơn 0")
    private Long discountValue;

    @Min(value = 0, message = "Giá trị đơn tối thiểu không được âm")
    private Long minOrderValue;

    private Long maxDiscountAmount;

    @NotNull(message = "Giới hạn sử dụng không được để trống")
    @Min(value = 1, message = "Giới hạn sử dụng phải ít nhất là 1")
    private Integer usageLimit;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải ở tương lai")
    private LocalDateTime endDate;
}