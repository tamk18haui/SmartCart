package com.gr6.SmartCart.modules.fulfillment.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.OrderDetailResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.OrderListResponse;
import com.gr6.SmartCart.modules.fulfillment.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các truy vấn liên quan đến đơn hàng của Seller.
 * Đáp ứng Luồng cơ bản 1, 2, 3 và Luồng rẽ nhánh 1, 2 của Use Case Quản lý đơn hàng.
 */
@RestController
@RequestMapping("/api/v1/shop-orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    /**
     * Lấy danh sách đơn hàng của Shop.
     * Hỗ trợ tìm kiếm theo từ khóa (Mã đơn hoặc tên khách hàng).
     *
     * @param keyword Từ khóa tìm kiếm (tùy chọn)
     * @return Danh sách các đơn hàng dưới dạng thẻ tóm tắt
     */
    @GetMapping
    public BaseResponse<List<OrderListResponse>> getAllOrders(
            @RequestParam(required = false) String keyword) {

        // Gọi Service để lấy dữ liệu.
        // Logic lọc theo Seller đã được xử lý bên trong Service thông qua SecurityContext.
        return orderQueryService.getAllOrders(keyword);
    }

    /**
     * Xem chi tiết một đơn hàng cụ thể.
     * Hệ thống sẽ tổng hợp thông tin từ bảng Addresses, Order_Items và Product_Variants.
     *
     * @param id ID của đơn hàng
     * @return Thông tin chi tiết đơn hàng, người nhận và danh sách sản phẩm
     */
    @GetMapping("/{id}")
    public BaseResponse<OrderDetailResponse> getOrderDetail(@PathVariable Long id) {

        // Service sẽ kiểm tra quyền sở hữu đơn hàng trước khi trả về dữ liệu.
        return orderQueryService.getOrderDetail(id);
    }
}