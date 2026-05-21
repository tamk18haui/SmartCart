package com.gr6.SmartCart.modules.fulfillment.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.*;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.PaymentMethod;
import com.gr6.SmartCart.common.enums.PaymentStatus;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.finance_core.repository.OrderRepository;
import com.gr6.SmartCart.modules.finance_core.repository.ShopOrderRepository;
import com.gr6.SmartCart.modules.finance_core.repository.TransactionRepository;
import com.gr6.SmartCart.modules.fulfillment.dto.CancelOrderRequest;
import com.gr6.SmartCart.modules.fulfillment.dto.UpdateShopOrderStatusRequest;
import com.gr6.SmartCart.modules.fulfillment.service.OrderActionService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderActionServiceImpl implements OrderActionService {

    private final ShopOrderRepository shopOrderRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }

        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    }

    private void checkPermission(ShopOrder shopOrder, User user, OrderStatus newStatus) {
        Order order = shopOrder.getOrder();

        boolean isBuyer = order.getUser().getUserId().equals(user.getUserId());
        boolean isSellerOwner = shopOrder.getShop().getUser().getUserId().equals(user.getUserId());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (isAdmin) {
            return;
        }

        if (newStatus == OrderStatus.COMPLETED) {
            if (!isBuyer) {
                throw new RuntimeException("Chỉ người mua mới được hoàn tất đơn hàng!");
            }
            return;
        }

        if (newStatus == OrderStatus.CANCELLED) {
            if (!isBuyer && !isSellerOwner) {
                throw new RuntimeException("Bạn không có quyền hủy đơn hàng này!");
            }
            return;
        }

        if (!isSellerOwner) {
            throw new RuntimeException("Chỉ seller của shop này mới được cập nhật trạng thái đơn hàng!");
        }
    }

    private void validateTransition(ShopOrder shopOrder, OrderStatus newStatus, String cancelReason) {
        OrderStatus currentStatus = shopOrder.getStatus();
        Order order = shopOrder.getOrder();

        if (currentStatus == newStatus) {
            throw new RuntimeException("Đơn hàng đã ở trạng thái này!");
        }

        if (currentStatus == OrderStatus.CANCELLED
                || currentStatus == OrderStatus.COMPLETED
                || currentStatus == OrderStatus.PAYMENT_FAILED) {
            throw new RuntimeException("Không thể cập nhật đơn hàng đã kết thúc!");
        }

        if (newStatus == OrderStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Không thể chuyển thủ công về trạng thái chờ thanh toán!");
        }

        if (currentStatus == OrderStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Đơn online chưa thanh toán, không thể cập nhật thủ công!");
        }

        switch (newStatus) {
            case CONFIRMED:
                if (currentStatus != OrderStatus.PENDING) {
                    throw new RuntimeException("Chỉ đơn PENDING mới được xác nhận!");
                }
                break;

            case SHIPPING:
                if (currentStatus != OrderStatus.CONFIRMED) {
                    throw new RuntimeException("Chỉ đơn CONFIRMED mới được chuyển sang SHIPPING!");
                }
                break;

            case DELIVERED:
                if (currentStatus != OrderStatus.SHIPPING) {
                    throw new RuntimeException("Chỉ đơn SHIPPING mới được chuyển sang DELIVERED!");
                }
                break;

            case COMPLETED:
                if (currentStatus != OrderStatus.DELIVERED) {
                    throw new RuntimeException("Chỉ đơn DELIVERED mới được hoàn tất!");
                }
                break;

            case CANCELLED:
                if (currentStatus == OrderStatus.SHIPPING
                        || currentStatus == OrderStatus.DELIVERED
                        || currentStatus == OrderStatus.COMPLETED) {
                    throw new RuntimeException("Không thể hủy đơn ở trạng thái hiện tại!");
                }

                if (cancelReason == null || cancelReason.isBlank()) {
                    throw new RuntimeException("Vui lòng nhập lý do hủy đơn!");
                }
                break;

            default:
                throw new RuntimeException("Trạng thái không hợp lệ!");
        }

        if (order.getPaymentMethod() == PaymentMethod.ONLINE
                && order.getPaymentStatus() != PaymentStatus.COMPLETED
                && newStatus != OrderStatus.CANCELLED) {
            throw new RuntimeException("Đơn online chưa thanh toán thành công!");
        }
    }

    private void restoreStock(ShopOrder shopOrder) {
        if (shopOrder.getItems() == null) {
            return;
        }

        for (OrderItem item : shopOrder.getItems()) {
            ProductVariant variant = variantRepository.findByIdWithLock(item.getVariant().getVariantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variantRepository.save(variant);
        }
    }

    private void handleSideEffects(ShopOrder shopOrder, OrderStatus newStatus, String cancelReason) {
        Order order = shopOrder.getOrder();
        OrderStatus currentStatus = shopOrder.getStatus();

        if (newStatus == OrderStatus.CANCELLED) {
            boolean stockWasDeducted =
                    currentStatus == OrderStatus.PENDING
                            || currentStatus == OrderStatus.CONFIRMED;

            if (stockWasDeducted) {
                restoreStock(shopOrder);
            }

            shopOrder.setCancelReason(cancelReason);

            if (order.getPaymentMethod() == PaymentMethod.ONLINE
                    && order.getPaymentStatus() == PaymentStatus.COMPLETED) {
                order.setPaymentStatus(PaymentStatus.REFUND_PENDING);

                Transaction transaction = transactionRepository.findByOrder_OrderId(order.getOrderId()).orElse(null);
                if (transaction != null) {
                    transaction.setStatus(PaymentStatus.REFUND_PENDING);
                    transactionRepository.save(transaction);
                }
            }
        }
    }

    private void syncParentOrderStatus(Order order) {
        List<ShopOrder> shopOrders = shopOrderRepository.findByOrder_OrderId(order.getOrderId());

        if (shopOrders.isEmpty()) {
            return;
        }

        boolean allCancelled = shopOrders.stream()
                .allMatch(o -> o.getStatus() == OrderStatus.CANCELLED);

        boolean allPaymentFailed = shopOrders.stream()
                .allMatch(o -> o.getStatus() == OrderStatus.PAYMENT_FAILED);

        boolean allCompleted = shopOrders.stream()
                .allMatch(o -> o.getStatus() == OrderStatus.COMPLETED);

        boolean allDeliveredOrCompleted = shopOrders.stream()
                .allMatch(o -> o.getStatus() == OrderStatus.DELIVERED
                        || o.getStatus() == OrderStatus.COMPLETED);

        boolean anyShipping = shopOrders.stream()
                .anyMatch(o -> o.getStatus() == OrderStatus.SHIPPING);

        boolean anyConfirmed = shopOrders.stream()
                .anyMatch(o -> o.getStatus() == OrderStatus.CONFIRMED);

        boolean anyPending = shopOrders.stream()
                .anyMatch(o -> o.getStatus() == OrderStatus.PENDING);

        if (allCancelled) {
            order.setStatus(OrderStatus.CANCELLED);
        } else if (allPaymentFailed) {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            order.setPaymentStatus(PaymentStatus.FAILED);
        } else if (allCompleted) {
            order.setStatus(OrderStatus.COMPLETED);

            if (order.getPaymentMethod() == PaymentMethod.COD) {
                order.setPaymentStatus(PaymentStatus.COMPLETED);
            }
        } else if (allDeliveredOrCompleted) {
            order.setStatus(OrderStatus.DELIVERED);
        } else if (anyShipping) {
            order.setStatus(OrderStatus.SHIPPING);
        } else if (anyConfirmed) {
            order.setStatus(OrderStatus.CONFIRMED);
        } else if (anyPending) {
            order.setStatus(OrderStatus.PENDING);
        }

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public BaseResponse<String> updateShopOrderStatus(
            Long shopOrderId,
            UpdateShopOrderStatusRequest request
    ) {
        User currentUser = getCurrentUser();

        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        OrderStatus newStatus = request.getStatus();

        checkPermission(shopOrder, currentUser, newStatus);
        validateTransition(shopOrder, newStatus, request.getCancelReason());
        handleSideEffects(shopOrder, newStatus, request.getCancelReason());

        shopOrder.setStatus(newStatus);
        shopOrderRepository.save(shopOrder);

        syncParentOrderStatus(shopOrder.getOrder());

        return BaseResponse.successMessage("Cập nhật trạng thái đơn hàng thành công!");
    }

    @Override
    @Transactional
    public BaseResponse<String> confirmOrder(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ShopOrder order = shopOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        if (!order.getShop().getUser().getEmail().equals(email)) {
            return BaseResponse.error(404, "Bạn không có quyền xử lý đơn hàng này!");
        }

        order.setStatus(OrderStatus.SHIPPING);
        shopOrderRepository.save(order);
        return BaseResponse.success("Xác nhận đơn hàng thành công!");
    }

    @Override
    @Transactional
    public BaseResponse<String> cancelOrder(Long orderId, CancelOrderRequest request) {
        // 1. Tìm đơn hàng con của Shop (Shop_Orders)
        ShopOrder shopOrder = shopOrderRepository.findById(orderId)
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

        shopOrderRepository.save(shopOrder);
        return BaseResponse.successMessage( "Đã hủy đơn và hoàn trả số lượng vào kho thành công.");
    }
}