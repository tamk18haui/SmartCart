package com.gr6.SmartCart.modules.fulfillment.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.ShopOrder;
import com.gr6.SmartCart.modules.finance_core.repository.ShopOrderRepository;
import com.gr6.SmartCart.modules.fulfillment.dto.*;
import com.gr6.SmartCart.modules.fulfillment.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final ShopOrderRepository orderRepository;

    @Override
    public BaseResponse<List<OrderListResponse>> getAllOrders(String keyword) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ShopOrder> orders = orderRepository.searchOrdersByShop(email, keyword);

        if (orders.isEmpty()) {
            return BaseResponse.success_data("Chưa có đơn hàng nào!",List.of());
        }

        List<OrderListResponse> response = orders.stream().map(order ->
                OrderListResponse.builder()
                        .id(order.getShopOrderId()) // Đã sửa từ id -> shopOrderId
                        .orderCode("ORD-" + order.getShopOrderId())
                        .totalAmount(BigDecimal.valueOf(order.getTotalAmount()))
                        .status(order.getStatus())
                        .build()
        ).collect(Collectors.toList());

        return BaseResponse.success(response);
    }

    @Override
    public BaseResponse<OrderDetailResponse> getOrderDetail(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ShopOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getShop().getUser().getEmail().equals(email)) {
            return BaseResponse.error(404, "Bạn không có quyền xem đơn hàng này");
        }

        // Lấy thông tin từ bảng Order (parentOrder) của bạn
        var parentOrder = order.getOrder();

        OrderDetailResponse detail = OrderDetailResponse.builder()
                .id(order.getShopOrderId())
                .status(order.getStatus())
                .totalAmount(BigDecimal.valueOf(order.getTotalAmount()))
                // LẤY TRỰC TIẾP TỪ parentOrder VÌ BẠN ĐÃ KHAI BÁO TRONG ENTITY
                .receiverName(parentOrder.getReceiverName())
                .receiverPhone(parentOrder.getReceiverPhone())
                .shippingAddress(parentOrder.getShippingAddress())
                .items(order.getItems().stream().map(item ->
                        OrderItemDTO.builder()
                                .productName(item.getVariant().getProduct().getName())
                                .variantName(item.getVariant().getSku())
                                .quantity(item.getQuantity())
                                .price(BigDecimal.valueOf(item.getPriceAtPurchase()))
                                .build()
                ).collect(Collectors.toList()))
                .build();

        return BaseResponse.success(detail);
    }
}