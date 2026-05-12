package com.gr6.SmartCart.module_v2.order_v2.service.impl;
import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.ShopOrder;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.modules.finance_core.repository.ShopOrderRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.module_v2.order_v2.dto.OrderHistoryResponse;
import com.gr6.SmartCart.module_v2.order_v2.dto.OrderTrackingResponse;
import com.gr6.SmartCart.module_v2.order_v2.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private final ShopOrderRepository shopOrderRepository;
    private final UserRepository userRepository;

    private User getCurrentBuyer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<OrderHistoryResponse>> getOrderHistory() {
        User user = getCurrentBuyer();
        
        List<ShopOrder> shopOrders = shopOrderRepository.findByOrder_User_EmailOrderByShopOrderIdDesc(user.getEmail());

        List<OrderHistoryResponse> responses = shopOrders.stream().map(order -> OrderHistoryResponse.builder()
                .shopOrderId(order.getShopOrderId())
                .shopName(order.getShop().getShopName())
                .status(order.getStatus())
                .totalAmount(BigDecimal.valueOf(order.getTotalAmount()))
                .createdAt(order.getOrder().getCreatedAt())
                .build()).collect(Collectors.toList());

        return BaseResponse.success_data("Lấy lịch sử đơn hàng thành công", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<OrderTrackingResponse> trackOrder(Long shopOrderId) {
        User user = getCurrentBuyer();
        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!shopOrder.getOrder().getUser().getUserId().equals(user.getUserId())) {
            return BaseResponse.error(403, "Bạn không có quyền xem đơn hàng này");
        }

        List<OrderTrackingResponse.TrackingItemDto> items = shopOrder.getItems().stream().map(item -> 
            OrderTrackingResponse.TrackingItemDto.builder()
                .productName(item.getVariant().getProduct().getName())
                .variantSku(item.getVariant().getSku())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .imageUrl(item.getVariant().getImageUrl() != null ? item.getVariant().getImageUrl() : item.getVariant().getProduct().getImageUrls())
                .build()
        ).collect(Collectors.toList());

        OrderTrackingResponse response = OrderTrackingResponse.builder()
                .shopOrderId(shopOrder.getShopOrderId())
                .shopName(shopOrder.getShop().getShopName())
                .status(shopOrder.getStatus())
                .totalAmount(BigDecimal.valueOf(shopOrder.getTotalAmount()))
                .shippingFee(BigDecimal.valueOf(shopOrder.getShippingFee()))
                .discountAmount(BigDecimal.valueOf(shopOrder.getDiscountAmount() != null ? shopOrder.getDiscountAmount() : 0))
                .receiverName(shopOrder.getOrder().getReceiverName())
                .shippingAddress(shopOrder.getOrder().getShippingAddress())
                .createdAt(shopOrder.getOrder().getCreatedAt())
                .items(items)
                .build();

        return BaseResponse.success_data("Chi tiết đơn hàng", response);
    }
}