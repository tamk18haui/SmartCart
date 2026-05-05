package com.gr6.SmartCart.modules.finance_core.dto;

import com.gr6.SmartCart.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Vui lòng chọn địa chỉ giao hàng")
    private Long addressId;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;

    @Valid
    @NotEmpty (message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    private List<ShopOrderDto> shopOrders;

    @Data
    public static class ShopOrderDto {
        @NotNull(message = "Thiếu thông tin shop")
        private Long shopId;

        private String voucherCode;

        @Valid
        @NotEmpty(message = "Shop này chưa có sản phẩm nào")
        private List<OrderItemDto> items;
    }
    @Data
    public static class OrderItemDto {
        @NotNull(message = "Thiếu thông tin biến thể sản phẩm")
        private Long variantId;

        @NotNull(message = "Thiếu số lượng")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer quantity;
    }
}
