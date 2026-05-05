package com.gr6.SmartCart.modules.finance_core.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutPreviewRequest;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutPreviewResponse;
import com.gr6.SmartCart.modules.finance_core.dto.CreateOrderRequest;
import com.gr6.SmartCart.modules.finance_core.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * API 1: Hiển thị màn hình Thanh toán (Preview)
     * - App gọi API này khi người dùng bắt đầu vào màn hình Đặt hàng.
     * - Tính toán phí ship, áp dụng voucher, tổng tiền, và tự động lấy địa chỉ.
     */
    @PostMapping("/preview")
    public BaseResponse<CheckoutPreviewResponse> getCheckoutPreview(
           @Valid @RequestBody CheckoutPreviewRequest request
    ) {
        return orderService.getCheckoutPreview(request);
    }

    /**
     * API 2: Chốt đặt hàng và trừ kho (Checkout)
     * - App gọi API này khi người dùng ấn nút "Đặt hàng" cuối cùng.
     * - Xử lý lưu Database, trừ tồn kho, và trả về link thanh toán Online (nếu chọn).
     */
    @PostMapping("/checkout")
    public BaseResponse<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return orderService.createOrder(request);
    }
}