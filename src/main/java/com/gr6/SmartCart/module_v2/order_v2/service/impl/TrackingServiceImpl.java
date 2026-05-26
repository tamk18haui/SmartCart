package com.gr6.SmartCart.module_v2.order_v2.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.*;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.PaymentMethod;
import com.gr6.SmartCart.common.enums.PaymentProvider;
import com.gr6.SmartCart.common.enums.PaymentStatus;
import com.gr6.SmartCart.module_v2.order_v2.dto.OrderHistoryResponse;
import com.gr6.SmartCart.module_v2.order_v2.dto.OrderTrackingResponse;
import com.gr6.SmartCart.module_v2.order_v2.service.TrackingService;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutOrderResponse;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCreateResult;
import com.gr6.SmartCart.modules.finance_core.repository.OrderRepository;
import com.gr6.SmartCart.modules.finance_core.repository.ShopOrderRepository;
import com.gr6.SmartCart.modules.finance_core.repository.TransactionRepository;
import com.gr6.SmartCart.modules.finance_core.service.PaymentGatewayService;
import com.gr6.SmartCart.modules.fulfillment.repository.ReviewRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private final ShopOrderRepository shopOrderRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentGatewayService paymentGatewayService;

    private User getCurrentBuyer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new RuntimeException("Bạn chưa đăng nhập");
        }

        String email = auth.getName();

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
                .filter(Objects::nonNull)
                .map(shopOrder -> OrderHistoryResponse.builder()
                        .orderId(shopOrder.getOrder() == null ? null : shopOrder.getOrder().getOrderId())
                        .shopOrderId(shopOrder.getShopOrderId())
                        .shopId(shopOrder.getShop() == null ? null : shopOrder.getShop().getShopId())
                        .shopName(shopOrder.getShop() == null ? "SmartCart Shop" : shopOrder.getShop().getShopName())
                        .status(shopOrder.getStatus())
                        .totalAmount(toBigDecimal(shopOrder.getTotalAmount()))
                        .createdAt(shopOrder.getOrder() == null ? null : shopOrder.getOrder().getCreatedAt())
                        .items(shopOrder.getItems() == null
                                ? List.of()
                                : shopOrder.getItems()
                                  .stream()
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

        if (shopOrderId == null || shopOrderId <= 0) {
            return BaseResponse.error(400, "Mã đơn hàng không hợp lệ");
        }

        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Order order = shopOrder.getOrder();

        if (order == null || order.getUser() == null) {
            return BaseResponse.error(400, "Dữ liệu đơn hàng không hợp lệ");
        }

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            return BaseResponse.error(403, "Bạn không có quyền xem đơn hàng này");
        }

        List<OrderTrackingResponse.TrackingItemDto> items = shopOrder.getItems() == null
                ? List.of()
                : shopOrder.getItems()
                  .stream()
                  .map(item -> {
                      boolean reviewed = false;

                      if (item.getOrderItemId() != null) {
                          reviewed = reviewRepository.existsByOrderItem_OrderItemId(item.getOrderItemId());
                      }

                      boolean canReview = isCompletedForReview(shopOrder) && !reviewed;

                      ProductVariant variant = item.getVariant();
                      Product product = variant == null ? null : variant.getProduct();

                      return OrderTrackingResponse.TrackingItemDto.builder()
                             .orderItemId(item.getOrderItemId())
                             .productId(product == null ? null : product.getProductId())
                             .variantId(variant == null ? null : variant.getVariantId())
                             .productName(product == null ? "Sản phẩm" : product.getName())
                             .variantSku(variant == null ? "" : safeString(variant.getSku()))
                             .quantity(item.getQuantity() == null ? 0 : item.getQuantity())
                             .priceAtPurchase(item.getPriceAtPurchase() == null ? 0L : item.getPriceAtPurchase())
                             .imageUrl(getBestImageUrl(variant))
                             .canReview(canReview)
                             .reviewed(reviewed)
                             .build();
                  })
                  .collect(Collectors.toList());

        OrderTrackingResponse response = OrderTrackingResponse.builder()
                .shopOrderId(shopOrder.getShopOrderId())
                .shopId(shopOrder.getShop() == null ? null : shopOrder.getShop().getShopId())
                .shopName(shopOrder.getShop() == null ? "SmartCart Shop" : shopOrder.getShop().getShopName())
                .status(shopOrder.getStatus())
                .totalAmount(toBigDecimal(shopOrder.getTotalAmount()))
                .shippingFee(toBigDecimal(shopOrder.getShippingFee()))
                .discountAmount(toBigDecimal(shopOrder.getDiscountAmount()))
                .paymentMethod(order.getPaymentMethod() == null ? null : order.getPaymentMethod().name())
                .paymentProvider(order.getPaymentProvider() == null ? null : order.getPaymentProvider().name())
                .paymentStatus(order.getPaymentStatus() == null ? null : order.getPaymentStatus().name())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getCreatedAt())
                .canCancel(shopOrder.getStatus() == OrderStatus.PENDING)
                .items(items)
                .build();

        return BaseResponse.success_data("Chi tiết đơn hàng", response);
    }

    @Override
    @Transactional
    public BaseResponse<String> completeBuyerOrder(Long shopOrderId) {
        User user = getCurrentBuyer();

        if (shopOrderId == null || shopOrderId <= 0) {
            return BaseResponse.error(400, "Mã đơn hàng không hợp lệ");
        }

        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Order order = shopOrder.getOrder();

        if (order == null || order.getUser() == null) {
            return BaseResponse.error(400, "Dữ liệu đơn hàng không hợp lệ");
        }

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            return BaseResponse.error(403, "Bạn không có quyền cập nhật đơn hàng này");
        }

        if (shopOrder.getStatus() != OrderStatus.DELIVERED) {
            return BaseResponse.error(400, "Chỉ đơn hàng đã giao mới được xác nhận hoàn thành");
        }

        shopOrder.setStatus(OrderStatus.COMPLETED);
        shopOrderRepository.save(shopOrder);

        if (order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        }

        syncParentOrderStatus(order.getOrderId());

        return BaseResponse.success_data("Đơn hàng đã hoàn thành", "OK");
    }

    @Override
    @Transactional
    public BaseResponse<CheckoutOrderResponse> retryPayment(Long shopOrderId) {
        User user = getCurrentBuyer();

        if (shopOrderId == null || shopOrderId <= 0) {
            return BaseResponse.error(400, "Mã đơn hàng không hợp lệ");
        }

        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Order order = shopOrder.getOrder();

        if (order == null || order.getUser() == null) {
            return BaseResponse.error(400, "Dữ liệu đơn hàng không hợp lệ");
        }

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            return BaseResponse.error(403, "Bạn không có quyền thanh toán đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT
                && shopOrder.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return BaseResponse.error(400, "Chỉ đơn hàng chờ thanh toán mới được thanh toán lại");
        }

        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            return BaseResponse.error(400, "Đơn hàng này không phải thanh toán online");
        }

        PaymentProvider provider = order.getPaymentProvider();

        if (provider == null || provider == PaymentProvider.NONE) {
            return BaseResponse.error(400, "Đơn hàng chưa có cổng thanh toán");
        }

        Transaction transaction = transactionRepository.findByOrder_OrderId(order.getOrderId())
                .orElseGet(() -> {
                    Transaction t = new Transaction();
                    t.setOrder(order);
                    return t;
                });

        Long amount = order.getTotalAmount() == null
                ? 0L
                : order.getTotalAmount().longValue();

        if (amount <= 0) {
            return BaseResponse.error(400, "Số tiền thanh toán không hợp lệ");
        }

        transaction.setAmount(amount);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction = transactionRepository.save(transaction);

        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        orderRepository.save(order);

        PaymentCreateResult paymentResult = paymentGatewayService.createPaymentUrl(
                order,
                transaction,
                provider
        );

        transaction.setProviderTransactionId(paymentResult.getProviderTransactionId());
        transactionRepository.save(transaction);

        CheckoutOrderResponse response = CheckoutOrderResponse.builder()
                .orderId(order.getOrderId())
                .transactionId(transaction.getTransactionId())
                .paymentUrl(paymentResult.getPaymentUrl())
                .orderStatus(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .paymentProvider(provider.name())
                .checkoutSource(order.getCheckoutSource() == null ? null : order.getCheckoutSource().name())
                .totalAmount(amount)
                .build();

        return BaseResponse.success_data("Tạo lại link thanh toán thành công", response);
    }

    private void syncParentOrderStatus(Long orderId) {
        if (orderId == null || orderId <= 0) {
            return;
        }

        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            return;
        }

        List<ShopOrder> shopOrders = shopOrderRepository.findByOrder_OrderId(orderId);

        if (shopOrders == null || shopOrders.isEmpty()) {
            return;
        }

        boolean allCompleted = shopOrders.stream()
                .filter(Objects::nonNull)
                .allMatch(so -> so.getStatus() == OrderStatus.COMPLETED);

        boolean allCancelledOrFailed = shopOrders.stream()
                .filter(Objects::nonNull)
                .allMatch(so ->
                        so.getStatus() == OrderStatus.CANCELLED
                                || so.getStatus() == OrderStatus.PAYMENT_FAILED
                );

        boolean allDeliveredOrCompleted = shopOrders.stream()
                .filter(Objects::nonNull)
                .allMatch(so ->
                        so.getStatus() == OrderStatus.DELIVERED
                                || so.getStatus() == OrderStatus.COMPLETED
                );

        boolean anyShipping = shopOrders.stream()
                .filter(Objects::nonNull)
                .anyMatch(so -> so.getStatus() == OrderStatus.SHIPPING);

        boolean anyPreparing = shopOrders.stream()
                .filter(Objects::nonNull)
                .anyMatch(so -> so.getStatus() == OrderStatus.PREPARING);

        boolean anyConfirmed = shopOrders.stream()
                .filter(Objects::nonNull)
                .anyMatch(so -> so.getStatus() == OrderStatus.CONFIRMED);

        boolean anyPending = shopOrders.stream()
                .filter(Objects::nonNull)
                .anyMatch(so -> so.getStatus() == OrderStatus.PENDING);

        if (allCompleted) {
            order.setStatus(OrderStatus.COMPLETED);
        } else if (allCancelledOrFailed) {
            order.setStatus(OrderStatus.CANCELLED);
        } else if (allDeliveredOrCompleted) {
            order.setStatus(OrderStatus.DELIVERED);
        } else if (anyShipping) {
            order.setStatus(OrderStatus.SHIPPING);
        } else if (anyPreparing) {
            order.setStatus(OrderStatus.PREPARING);
        } else if (anyConfirmed) {
            order.setStatus(OrderStatus.CONFIRMED);
        } else if (anyPending) {
            order.setStatus(OrderStatus.PENDING);
        }

        orderRepository.save(order);
    }

    private boolean isCompletedForReview(ShopOrder shopOrder) {
        if (shopOrder == null) {
            return false;
        }

        if (shopOrder.getStatus() == OrderStatus.COMPLETED) {
            return true;
        }

        return shopOrder.getOrder() != null
                && shopOrder.getOrder().getStatus() == OrderStatus.COMPLETED;
    }

    private OrderHistoryResponse.OrderHistoryItemResponse mapHistoryItem(OrderItem item) {
        if (item == null) {
            return OrderHistoryResponse.OrderHistoryItemResponse.builder()
                    .productName("Sản phẩm")
                    .variantSku("")
                    .quantity(0)
                    .priceAtPurchase(0L)
                    .canReview(false)
                    .reviewed(false)
                    .build();
        }

        ProductVariant variant = item.getVariant();
        Product product = variant == null ? null : variant.getProduct();

        boolean reviewed = false;

        if (item.getOrderItemId() != null) {
            reviewed = reviewRepository.existsByOrderItem_OrderItemId(item.getOrderItemId());
        }

        ShopOrder shopOrder = item.getShopOrder();

        boolean canReview = isCompletedForReview(shopOrder) && !reviewed;

        return OrderHistoryResponse.OrderHistoryItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .productId(product == null ? null : product.getProductId())
                .variantId(variant == null ? null : variant.getVariantId())
                .productName(product == null ? "Sản phẩm" : safeString(product.getName()))
                .variantSku(variant == null ? "" : safeString(variant.getSku()))
                .quantity(item.getQuantity() == null ? 0 : item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase() == null ? 0L : item.getPriceAtPurchase())
                .imageUrl(getBestImageUrl(variant))
                .canReview(canReview)
                .reviewed(reviewed)
                .build();
    }

    private String getBestImageUrl(ProductVariant variant) {
        if (variant == null) {
            return null;
        }

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

    private BigDecimal toBigDecimal(Long value) {
        return BigDecimal.valueOf(value == null ? 0L : value);
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}