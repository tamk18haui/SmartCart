package com.gr6.SmartCart.modules.finance_core.dto;

import com.gr6.SmartCart.common.enums.CheckoutSource;
import com.gr6.SmartCart.common.enums.PaymentMethod;
import com.gr6.SmartCart.common.enums.PaymentProvider;
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

    /**
     * COD: có thể để NONE hoặc null.
     * ONLINE: bắt buộc MOMO hoặc VNPAY.
     */
    private PaymentProvider paymentProvider;

    @NotNull(message = "Vui lòng chọn nguồn mua hàng")
    private CheckoutSource checkoutSource;

    /**
     * Để sẵn cho chống double-click tạo trùng đơn.
     * Bản này chưa enforce unique DB.
     */
    private String checkoutToken;

    @Valid
    @NotEmpty(message = "Danh sách đơn hàng không được để trống")
    private List<ShopOrderRequest> shopOrders;

    @Data
    public static class ShopOrderRequest {

        @NotNull(message = "Thiếu thông tin shop")
        private Long shopId;

        private String voucherCode;

        @Valid
        @NotEmpty(message = "Shop này chưa có sản phẩm nào")
        private List<ItemRequest> items;
    }

    @Data
    public static class ItemRequest {

        @NotNull(message = "Thiếu thông tin biến thể sản phẩm")
        private Long variantId;

        @NotNull(message = "Thiếu số lượng")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer quantity;
    }
}