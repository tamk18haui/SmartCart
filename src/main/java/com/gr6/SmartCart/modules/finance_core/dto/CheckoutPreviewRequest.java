package com.gr6.SmartCart.modules.finance_core.dto;

import com.gr6.SmartCart.common.enums.CheckoutSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutPreviewRequest {
    private Long addressId;

    @NotNull(message = "Vui lòng chọn nguồn mua hàng")
    private CheckoutSource checkoutSource;

    @Valid
    @NotEmpty(message = "Danh sách đơn hàng không được để trống")
    private List<ShopOrderPreviewDto> shopOrders;

    @Data
    public static class ShopOrderPreviewDto {
        @NotNull(message = "Thiếu thông tin shop")
        private Long shopId;

        private String voucherCode;

        @Valid
        @NotEmpty(message = "Shop này chưa có sản phẩm nào")
        private List<ItemPreviewDto> items;
    }

    @Data
    public static class ItemPreviewDto {
        @NotNull(message = "Thiếu thông tin biến thể sản phẩm")
        private Long variantId;

        @NotNull(message = "Thiếu số lượng")
        @Min(value = 1, message = "Số lượng mua phải lớn hơn 0")
        private Integer quantity;
    }
}