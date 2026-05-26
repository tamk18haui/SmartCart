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
import com.gr6.SmartCart.modules.notification.service.AppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import com.gr6.SmartCart.common.enums.SettlementStatus;
import com.gr6.SmartCart.common.enums.WalletStatus;
import com.gr6.SmartCart.common.enums.WalletTransactionType;
import com.gr6.SmartCart.module_v3.withdraw.repository.SellerSettlementRepository;
import com.gr6.SmartCart.module_v3.withdraw.repository.WithdrawWalletRepository;
import com.gr6.SmartCart.module_v3.withdraw.repository.WithdrawWalletTransactionRepository;

@Service
@RequiredArgsConstructor
public class OrderActionServiceImpl implements OrderActionService {

    private final ShopOrderRepository shopOrderRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private static final int PLATFORM_COMMISSION_PERCENT = 0;

    private final SellerSettlementRepository settlementRepository;
    private final WithdrawWalletRepository walletRepository;
    private final WithdrawWalletTransactionRepository walletTransactionRepository;
    private final AppNotificationService appNotificationService;

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

    private void notifyOrderStatusChanged(ShopOrder shopOrder, User actor, OrderStatus newStatus) {
        if (shopOrder == null || shopOrder.getOrder() == null) {
            return;
        }

        User buyer = shopOrder.getOrder().getUser();
        User seller = shopOrder.getShop() == null ? null : shopOrder.getShop().getUser();

        String statusText = newStatus == null ? "" : newStatus.name();

        if (buyer != null && (actor == null || !buyer.getUserId().equals(actor.getUserId()))) {
            Map<String, String> buyerData = new java.util.HashMap<>();
            buyerData.put("type", "ORDER");
            buyerData.put("routeKey", "BUYER_ORDER_DETAIL");
            buyerData.put("targetId", String.valueOf(shopOrder.getShopOrderId()));
            buyerData.put("shopOrderId", String.valueOf(shopOrder.getShopOrderId()));
            buyerData.put("status", statusText);

            if (shopOrder.getOrder() != null && shopOrder.getOrder().getOrderId() != null) {
                buyerData.put("orderId", String.valueOf(shopOrder.getOrder().getOrderId()));
            }

            appNotificationService.notifyUser(
                    buyer.getUserId(),
                    "Đơn hàng đã cập nhật",
                    "Đơn #" + shopOrder.getShopOrderId() + " đã chuyển sang trạng thái " + statusText,
                    com.gr6.SmartCart.common.enums.NotificationType.ORDER,
                    "BUYER_ORDER_DETAIL",
                    shopOrder.getShopOrderId(),
                    null,
                    buyerData
            );
        }

        if (seller != null && (actor == null || !seller.getUserId().equals(actor.getUserId()))) {
            Map<String, String> sellerData = new java.util.HashMap<>();
            sellerData.put("type", "ORDER");
            sellerData.put("routeKey", "SELLER_ORDER_DETAIL");
            sellerData.put("targetId", String.valueOf(shopOrder.getShopOrderId()));
            sellerData.put("shopOrderId", String.valueOf(shopOrder.getShopOrderId()));
            sellerData.put("status", statusText);

            if (shopOrder.getOrder() != null && shopOrder.getOrder().getOrderId() != null) {
                sellerData.put("orderId", String.valueOf(shopOrder.getOrder().getOrderId()));
            }

            appNotificationService.notifyUser(
                    seller.getUserId(),
                    "Đơn hàng đã cập nhật",
                    "Đơn #" + shopOrder.getShopOrderId() + " đã chuyển sang trạng thái " + statusText,
                    com.gr6.SmartCart.common.enums.NotificationType.ORDER,
                    "SELLER_ORDER_DETAIL",
                    shopOrder.getShopOrderId(),
                    null,
                    sellerData
            );
        }
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
        shopOrder = shopOrderRepository.save(shopOrder);

        notifyOrderStatusChanged(shopOrder, currentUser, newStatus);

        if (newStatus == OrderStatus.COMPLETED) {
            settleCompletedShopOrder(shopOrder);
        }

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
        ShopOrder shopOrder = shopOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        shopOrder.setStatus(OrderStatus.CANCELLED);
        shopOrder.setCancelReason(request.getCancelReason());

        if (shopOrder.getItems() != null) {
            for (OrderItem item : shopOrder.getItems()) {
                var variant = item.getVariant();
                if (variant != null) {
                    int newStock = variant.getStockQuantity() + item.getQuantity();
                    variant.setStockQuantity(newStock);
                }
            }
        }

        shopOrderRepository.save(shopOrder);
        return BaseResponse.successMessage("Đã hủy đơn và hoàn trả số lượng vào kho thành công.");
    }

    private void settleCompletedShopOrder(ShopOrder shopOrder) {
        if (shopOrder.getStatus() != OrderStatus.COMPLETED) {
            return;
        }

        if (settlementRepository.existsByShopOrder_ShopOrderId(shopOrder.getShopOrderId())) {
            return;
        }

        Shop shop = shopOrder.getShop();
        User seller = shop.getUser();

        getOrCreateWallet(seller);

        Wallet lockedWallet = walletRepository.findByUserIdForUpdate(seller.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví seller"));

        long gross = shopOrder.getTotalAmount() == null ? 0L : shopOrder.getTotalAmount();
        long commission = gross * PLATFORM_COMMISSION_PERCENT / 100;
        long net = gross - commission;

        lockedWallet.setBalance(
                (lockedWallet.getBalance() == null ? 0L : lockedWallet.getBalance()) + net
        );
        walletRepository.save(lockedWallet);

        SellerSettlement settlement = SellerSettlement.builder()
                .shopOrder(shopOrder)
                .seller(seller)
                .shop(shop)
                .grossAmount(gross)
                .commissionAmount(commission)
                .netAmount(net)
                .status(SettlementStatus.SETTLED)
                .note("Tự động cộng tiền khi shopOrder #" + shopOrder.getShopOrderId() + " hoàn thành")
                .settledBy("AUTO_SYSTEM")
                .build();

        settlementRepository.save(settlement);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(lockedWallet);
        tx.setType(WalletTransactionType.TOP_UP);
        tx.setAmount(net);
        tx.setDescription("Tự động cộng tiền từ shopOrder #" + shopOrder.getShopOrderId());

        walletTransactionRepository.save(tx);
    }

    private Wallet getOrCreateWallet(User seller) {
        return walletRepository.findByUser_UserId(seller.getUserId())
                .orElseGet(() -> {
                    Wallet wallet = new Wallet();
                    wallet.setUser(seller);
                    wallet.setBalance(0L);
                    wallet.setStatus(WalletStatus.ACTIVE);
                    return walletRepository.save(wallet);
                });
    }
}


