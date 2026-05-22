package com.gr6.SmartCart.module_v2.order_v2.service.impl;
import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.ShopOrder;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.modules.finance_core.repository.ShopOrderRepository;
import com.gr6.SmartCart.modules.fulfillment.repository.ReviewRepository;
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
import com.gr6.SmartCart.common.domain.OrderItem;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;

@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private final ShopOrderRepository shopOrderRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    private User getCurrentBuyer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<OrderHistoryResponse>> getOrderHistory() {
        User user = getCurrentBuyer();

        List<ShopOrder> shopOrders =
                shopOrderRepository.findByOrder_User_EmailOrderByShopOrderIdDesc(user.getEmail());

        List<OrderHistoryResponse> responses = shopOrders.stream()
                .map(shopOrder -> OrderHistoryResponse.builder()
                        .orderId(shopOrder.getOrder().getOrderId())
                        .shopOrderId(shopOrder.getShopOrderId())
                        .shopId(shopOrder.getShop().getShopId())
                        .shopName(shopOrder.getShop().getShopName())
                        .status(shopOrder.getStatus())
                        .totalAmount(BigDecimal.valueOf(
                                shopOrder.getTotalAmount() == null ? 0L : shopOrder.getTotalAmount()
                        ))
                        .createdAt(shopOrder.getOrder().getCreatedAt())
                        .items(shopOrder.getItems().stream()
                                .map(this::mapHistoryItem)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return BaseResponse.success_data(
                "Lấy lịch sử đơn hàng thành công",
                responses
        );
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

        List<OrderTrackingResponse.TrackingItemDto> items = shopOrder.getItems()
                .stream()
                .map(item -> {
                    boolean reviewed = reviewRepository.existsByOrderItem_OrderItemId(item.getOrderItemId());
                    boolean canReview = shopOrder.getStatus() == OrderStatus.COMPLETED && !reviewed;

                    return OrderTrackingResponse.TrackingItemDto.builder()
                            .orderItemId(item.getOrderItemId())
                            .productId(item.getVariant().getProduct().getProductId())
                            .variantId(item.getVariant().getVariantId())
                            .productName(item.getVariant().getProduct().getName())
                            .variantSku(item.getVariant().getSku())
                            .quantity(item.getQuantity())
                            .priceAtPurchase(item.getPriceAtPurchase())
                            .imageUrl(item.getVariant().getImageUrl())
                            .canReview(canReview)
                            .reviewed(reviewed)
                            .build();
                })
                .toList();

        OrderTrackingResponse response = OrderTrackingResponse.builder()
                .shopOrderId(shopOrder.getShopOrderId())
                .shopName(shopOrder.getShop().getShopName())
                .status(shopOrder.getStatus())
                .totalAmount(BigDecimal.valueOf(shopOrder.getTotalAmount()))
                .shippingFee(BigDecimal.valueOf(shopOrder.getShippingFee()))
                .paymentMethod(shopOrder.getOrder().getPaymentMethod() == null ? null : shopOrder.getOrder().getPaymentMethod().name())
                .paymentProvider(shopOrder.getOrder().getPaymentProvider() == null ? null : shopOrder.getOrder().getPaymentProvider().name())
                .discountAmount(BigDecimal.valueOf(shopOrder.getDiscountAmount() != null ? shopOrder.getDiscountAmount() : 0))
                .receiverName(shopOrder.getOrder().getReceiverName())
                .receiverPhone(shopOrder.getOrder().getReceiverPhone())
                .shippingAddress(shopOrder.getOrder().getShippingAddress())
                .createdAt(shopOrder.getOrder().getCreatedAt())
                .canCancel(shopOrder.getStatus() == OrderStatus.PENDING)
                .items(items)
                .build();

        return BaseResponse.success_data("Chi tiết đơn hàng", response);
    }
    private OrderHistoryResponse.OrderHistoryItemResponse mapHistoryItem(OrderItem item) {
        ProductVariant variant = item.getVariant();
        Product product = variant.getProduct();

        return OrderHistoryResponse.OrderHistoryItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .productId(product.getProductId())
                .variantId(variant.getVariantId())
                .productName(product.getName())
                .variantSku(variant.getSku())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .imageUrl(getBestImageUrl(variant))
                .build();
    }

    private String getBestImageUrl(ProductVariant variant) {
        if (variant == null) return null;

        if (!isBlank(variant.getImageUrl())) {
            return variant.getImageUrl().trim();
        }

        Product product = variant.getProduct();

        if (product == null || isBlank(product.getImageUrls())) {
            return null;
        }

        String[] images = product.getImageUrls().split(",");

        for (String image : images) {
            if (!isBlank(image)) {
                return image.trim();
            }
        }

        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}