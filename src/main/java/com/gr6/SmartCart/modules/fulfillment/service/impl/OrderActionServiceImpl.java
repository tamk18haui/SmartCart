package com.gr6.SmartCart.modules.fulfillment.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.ShopOrder;
import com.gr6.SmartCart.common.domain.OrderItem;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.modules.fulfillment.dto.CancelOrderRequest;
import com.gr6.SmartCart.modules.fulfillment.repository.ShopOrderRepository;
import com.gr6.SmartCart.modules.fulfillment.service.OrderActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderActionServiceImpl implements OrderActionService {

    private final ShopOrderRepository orderRepository;

    @Override
    @Transactional
    public BaseResponse<String> confirmOrder(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ShopOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        if (!order.getShop().getUser().getEmail().equals(email)) {
            return BaseResponse.error(404, "Bạn không có quyền xử lý đơn hàng này!");
        }

        order.setStatus(OrderStatus.SHIPPING);
        orderRepository.save(order);
        return BaseResponse.success("Xác nhận đơn hàng thành công!");
    }

    @Override
    @Transactional
    public BaseResponse<String> cancelOrder(Long orderId, CancelOrderRequest request) {
        // 1. Tìm đơn hàng con của Shop (Shop_Orders)
        ShopOrder shopOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        // 2. Cập nhật trạng thái sang CANCELLED và lưu lý do
        shopOrder.setStatus(OrderStatus.CANCELLED);
        shopOrder.setCancelReason(request.getCancelReason());

        // 3. DUYỆT DANH SÁCH MÓN HÀNG (Order_Items) để hoàn kho
        if (shopOrder.getItems() != null) {
            for (OrderItem item : shopOrder.getItems()) {
                var variant = item.getVariant(); // Đổi getProductVariant() -> getVariant()
                if (variant != null) {
                    int newStock = variant.getStockQuantity() + item.getQuantity();
                    variant.setStockQuantity(newStock);
                }
            }
        }

        orderRepository.save(shopOrder);
        return BaseResponse.successMessage( "Đã hủy đơn và hoàn trả số lượng vào kho thành công.");
    }
}