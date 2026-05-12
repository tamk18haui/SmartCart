package com.gr6.SmartCart.module_v2.order_v2.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.OrderItem;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.ShopOrder;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.finance_core.repository.ShopOrderRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.module_v2.order_v2.dto.RefundRequest;
import com.gr6.SmartCart.module_v2.order_v2.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final ShopOrderRepository shopOrderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BaseResponse<String> cancelOrder(Long shopOrderId, RefundRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!shopOrder.getOrder().getUser().getUserId().equals(user.getUserId())) {
            return BaseResponse.error(403, "Không có quyền thao tác trên đơn hàng này");
        }

        if (shopOrder.getStatus() != OrderStatus.PENDING) {
            return BaseResponse.error(400, "Chỉ có thể hủy đơn hàng ở trạng thái chờ xác nhận");
        }

        shopOrder.setStatus(OrderStatus.CANCELLED);
        shopOrder.setCancelReason("Người mua hủy: " + request.getReason());

        // Sử dụng Lock bi quan (nếu repository hỗ trợ) để tránh Race Condition khi hoàn kho
        if (shopOrder.getItems() != null) {
            for (OrderItem item : shopOrder.getItems()) {
                ProductVariant variant = productVariantRepository.findByIdWithLock(item.getVariant().getVariantId())
                        .orElse(item.getVariant());
                variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                productVariantRepository.save(variant);
            }
        }

        shopOrderRepository.save(shopOrder);
        return BaseResponse.successMessage("Đã hủy đơn hàng và hoàn lại tồn kho");
    }
}