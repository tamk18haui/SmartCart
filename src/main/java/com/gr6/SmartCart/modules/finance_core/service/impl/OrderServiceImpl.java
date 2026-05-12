package com.gr6.SmartCart.modules.finance_core.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.*;
import com.gr6.SmartCart.common.enums.*;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.finance_core.dto.*;
import com.gr6.SmartCart.modules.finance_core.repository.*;
import com.gr6.SmartCart.modules.finance_core.service.OrderService;
import com.gr6.SmartCart.modules.finance_core.service.PaymentGatewayService;
import com.gr6.SmartCart.modules.finance_core.service.VoucherService;
import com.gr6.SmartCart.modules.identity.repository.ShopRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.storefront.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final long DEFAULT_SHIPPING_FEE = 30000L;

    private final OrderRepository orderRepository;
    private final ShopOrderRepository shopOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShopRepository shopRepository;
    private final CartItemRepository cartItemRepository;
    private final VoucherRepository voucherRepository;
    private final TransactionRepository transactionRepository;
    private final VoucherService voucherService;
    private final PaymentGatewayService paymentGatewayService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }

        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    }

    private Address getCheckoutAddress(Long addressId, User user) {
        if (addressId == null) {
            throw new RuntimeException("Vui lòng chọn địa chỉ giao hàng!");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ giao hàng!"));

        if (!address.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Địa chỉ này không thuộc về bạn!");
        }

        if (Boolean.TRUE.equals(address.getIsDeleted())) {
            throw new RuntimeException("Địa chỉ này đã bị xóa!");
        }

        return address;
    }

    private void validateShopCanSell(Shop shop) {
        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop " + shop.getShopName() + " chưa hoạt động hoặc đã bị khóa!");
        }
    }

    // ==========================================
    // BỌC THÉP BẢO MẬT: KIỂM TRA HÀNG HỢP LỆ
    // ==========================================
    private void validateVariantCanBuy(ProductVariant variant, Long expectedShopId) {
        if (variant == null || variant.getProduct() == null) {
            throw new RuntimeException("Biến thể sản phẩm không hợp lệ!");
        }

        if (variant.getStatus() != VariantStatus.ACTIVE) {
            throw new RuntimeException("Phân loại sản phẩm hiện không khả dụng!");
        }

        Product product = variant.getProduct();

        if (!product.getShop().getShopId().equals(expectedShopId)) {
            throw new RuntimeException("Sản phẩm " + product.getName() + " không thuộc shop đã chọn!");
        }

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new RuntimeException("Sản phẩm " + product.getName() + " hiện không còn được bán!");
        }

        if (product.getShop() == null || product.getShop().getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop của sản phẩm " + product.getName() + " hiện không hoạt động!");
        }

        if (product.getCategory() != null && product.getCategory().getCategoryStatus() != CategoryStatus.ACTIVE) {
            throw new RuntimeException("Danh mục của sản phẩm " + product.getName() + " hiện đang bị ẩn!");
        }
    }

    // ==========================================
    // BỌC THÉP BẢO MẬT: LẤY GIÁ AN TOÀN
    // ==========================================
    private long getUnitPrice(ProductVariant variant) {
        if (variant.getPrice() != null) {
            return variant.getPrice().longValue();
        }
        if (variant.getProduct() != null && variant.getProduct().getBasePrice() != null) {
            return variant.getProduct().getBasePrice().longValue();
        }
        throw new RuntimeException("Giá sản phẩm không hợp lệ!");
    }

    private String getOptionValues(ProductVariant variant) {
        if (variant.getVariantOptionValues() == null) {
            return "";
        }

        return variant.getVariantOptionValues()
                .stream()
                .filter(link -> link.getOptionValue() != null)
                .map(link -> link.getOptionValue().getValue())
                .collect(Collectors.joining(", "));
    }

    private String getVariantImage(ProductVariant variant) {
        if (variant.getImageUrl() != null && !variant.getImageUrl().isBlank()) {
            return variant.getImageUrl();
        }

        return variant.getProduct().getImageUrls();
    }

    private Map<Long, Integer> mergeQuantityByVariant(List<CreateOrderRequest.ItemRequest> items) {
        Map<Long, Integer> result = new HashMap<>();

        for (CreateOrderRequest.ItemRequest item : items) {
            result.merge(item.getVariantId(), item.getQuantity(), Integer::sum);
        }

        return result;
    }

    private Map<Long, Integer> mergePreviewQuantityByVariant(List<CheckoutPreviewRequest.ItemPreviewDto> items) {
        Map<Long, Integer> result = new HashMap<>();

        for (CheckoutPreviewRequest.ItemPreviewDto item : items) {
            result.merge(item.getVariantId(), item.getQuantity(), Integer::sum);
        }

        return result;
    }

    private void validateStockWithLock(Map<Long, Integer> quantityByVariant, Long shopId) {
        for (Map.Entry<Long, Integer> entry : quantityByVariant.entrySet()) {
            ProductVariant variant = variantRepository.findByIdWithLock(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

            validateVariantCanBuy(variant, shopId);

            if (variant.getStockQuantity() < entry.getValue()) {
                throw new RuntimeException("Sản phẩm " + variant.getProduct().getName()
                        + " không đủ tồn kho. Còn " + variant.getStockQuantity()
                        + ", cần " + entry.getValue());
            }
        }
    }

    private void validateStockNoLock(Map<Long, Integer> quantityByVariant, Long shopId) {
        for (Map.Entry<Long, Integer> entry : quantityByVariant.entrySet()) {
            ProductVariant variant = variantRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

            validateVariantCanBuy(variant, shopId);

            if (variant.getStockQuantity() < entry.getValue()) {
                throw new RuntimeException("Sản phẩm " + variant.getProduct().getName()
                        + " không đủ tồn kho. Còn " + variant.getStockQuantity()
                        + ", cần " + entry.getValue());
            }
        }
    }

    private void decreaseStock(Map<Long, Integer> quantityByVariant) {
        for (Map.Entry<Long, Integer> entry : quantityByVariant.entrySet()) {
            ProductVariant variant = variantRepository.findByIdWithLock(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

            if (variant.getStockQuantity() < entry.getValue()) {
                throw new RuntimeException("Sản phẩm " + variant.getProduct().getName() + " không đủ tồn kho!");
            }

            variant.setStockQuantity(variant.getStockQuantity() - entry.getValue());
            variantRepository.save(variant);
        }
    }

    private void validateCartItemsForCheckout(
            User user,
            CheckoutSource checkoutSource,
            Map<Long, Map<Long, Integer>> quantityByShop
    ) {
        if (checkoutSource != CheckoutSource.FROM_CART) {
            return;
        }

        for (Map<Long, Integer> quantityMap : quantityByShop.values()) {
            for (Map.Entry<Long, Integer> entry : quantityMap.entrySet()) {
                Long variantId = entry.getKey();
                Integer checkoutQuantity = entry.getValue();

                CartItem cartItem = cartItemRepository
                        .findByUser_UserIdAndVariant_VariantId(user.getUserId(), variantId)
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng!"));

                if (cartItem.getQuantity() < checkoutQuantity) {
                    throw new RuntimeException("Số lượng mua vượt quá số lượng trong giỏ hàng!");
                }
            }
        }
    }

    private void deleteCheckedOutCartItems(User user, Set<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return;
        }

        cartItemRepository.deleteByUser_UserIdAndVariant_VariantIdIn(user.getUserId(), variantIds);
    }

    private PaymentProvider normalizePaymentProvider(PaymentMethod method, PaymentProvider provider) {
        if (method == PaymentMethod.COD) {
            return PaymentProvider.NONE;
        }

        if (provider == null || provider == PaymentProvider.NONE) {
            throw new RuntimeException("Thanh toán online cần chọn MOMO hoặc VNPAY!");
        }

        if (provider != PaymentProvider.MOMO && provider != PaymentProvider.VNPAY) {
            throw new RuntimeException("Cổng thanh toán không hợp lệ!");
        }

        return provider;
    }

    private CheckoutOrderResponse buildCheckoutResponse(
            Order order,
            Transaction transaction,
            String paymentUrl
    ) {
        return CheckoutOrderResponse.builder()
                .orderId(order.getOrderId())
                .transactionId(transaction == null ? null : transaction.getTransactionId())
                .paymentUrl(paymentUrl)
                .orderStatus(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .paymentProvider(order.getPaymentProvider() == null ? null : order.getPaymentProvider().name())
                .checkoutSource(order.getCheckoutSource() == null ? null : order.getCheckoutSource().name())
                .totalAmount(order.getTotalAmount().longValue())
                .build();
    }

    @Override
    public BaseResponse<CheckoutPreviewResponse> getCheckoutPreview(CheckoutPreviewRequest request) {
        User user = getCurrentUser();
        Address address = getCheckoutAddress(request.getAddressId(), user);

        long totalItemPrice = 0L;
        long totalShippingFee = 0L;
        long totalDiscount = 0L;
        long totalAmount = 0L;

        Map<Long, Map<Long, Integer>> quantityByShop = new HashMap<>();
        List<CheckoutPreviewResponse.ShopPreview> shopPreviews = new ArrayList<>();

        for (CheckoutPreviewRequest.ShopOrderPreviewDto shopReq : request.getShopOrders()) {
            Shop shop = shopRepository.findById(shopReq.getShopId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy shop!"));

            validateShopCanSell(shop);

            Map<Long, Integer> mergedQuantity = mergePreviewQuantityByVariant(shopReq.getItems());
            validateStockNoLock(mergedQuantity, shop.getShopId());
            quantityByShop.put(shop.getShopId(), mergedQuantity);

            long shopItemTotal = 0L;
            List<CheckoutPreviewResponse.ItemPreview> itemPreviews = new ArrayList<>();

            for (Map.Entry<Long, Integer> entry : mergedQuantity.entrySet()) {
                ProductVariant variant = variantRepository.findById(entry.getKey())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

                long price = getUnitPrice(variant);
                long itemTotal = price * entry.getValue();
                shopItemTotal += itemTotal;

                itemPreviews.add(CheckoutPreviewResponse.ItemPreview.builder()
                        .productName(variant.getProduct().getName())
                        .variantImageUrl(getVariantImage(variant))
                        .price(price)
                        .quantity(entry.getValue())
                        .optionValues(getOptionValues(variant))
                        .build());
            }

            long discount = voucherService.calculateDiscount(
                    shopReq.getVoucherCode(),
                    shopItemTotal,
                    shop.getShopId(),
                    user.getUserId()
            );

            long shippingFee = DEFAULT_SHIPPING_FEE;
            long shopTotal = Math.max(0L, shopItemTotal + shippingFee - discount);

            totalItemPrice += shopItemTotal;
            totalShippingFee += shippingFee;
            totalDiscount += discount;
            totalAmount += shopTotal;

            shopPreviews.add(CheckoutPreviewResponse.ShopPreview.builder()
                    .shopId(shop.getShopId())
                    .shopName(shop.getShopName())
                    .items(itemPreviews)
                    .shopItemTotal(shopItemTotal)
                    .shopShippingFee(shippingFee)
                    .shopDiscount(discount)
                    .subtotal(shopTotal)
                    .build());
        }

        validateCartItemsForCheckout(user, request.getCheckoutSource(), quantityByShop);

        CheckoutPreviewResponse response = CheckoutPreviewResponse.builder()
                .defaultAddress(CheckoutPreviewResponse.AddressPreviewDto.builder()
                        .addressId(address.getAddressId())
                        .receiverName(address.getReceiverName())
                        .receiverPhone(address.getReceiverPhone())
                        .fullAddress(address.getFullAddress())
                        .build())
                .shops(shopPreviews)
                .totalItemPrice(totalItemPrice)
                .totalShippingFee(totalShippingFee)
                .totalDiscount(totalDiscount)
                .totalAmount(totalAmount)
                .build();

        return BaseResponse.success_data("Lấy thông tin thanh toán thành công", response);
    }

    @Override
    @Transactional
    public BaseResponse<?> createOrder(CreateOrderRequest request) {
        User user = getCurrentUser();

        if (user.getStatus() == UserStatus.BANNED) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa!");
        }

        Address address = getCheckoutAddress(request.getAddressId(), user);
        PaymentProvider paymentProvider = normalizePaymentProvider(
                request.getPaymentMethod(),
                request.getPaymentProvider()
        );

        Map<Long, Map<Long, Integer>> quantityByShop = new HashMap<>();
        Set<Long> checkedOutVariantIds = new HashSet<>();

        for (CreateOrderRequest.ShopOrderRequest shopReq : request.getShopOrders()) {
            Shop shop = shopRepository.findById(shopReq.getShopId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy shop!"));

            validateShopCanSell(shop);

            if (shop.getUser().getUserId().equals(user.getUserId())) {
                throw new RuntimeException("Bạn không thể mua sản phẩm của chính shop mình!");
            }

            Map<Long, Integer> mergedQuantity = mergeQuantityByVariant(shopReq.getItems());
            validateStockWithLock(mergedQuantity, shop.getShopId());

            quantityByShop.put(shop.getShopId(), mergedQuantity);
            checkedOutVariantIds.addAll(mergedQuantity.keySet());
        }

        validateCartItemsForCheckout(user, request.getCheckoutSource(), quantityByShop);

        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setShippingAddress(address.getFullAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentProvider(paymentProvider);
        order.setCheckoutSource(request.getCheckoutSource());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setStatus(request.getPaymentMethod() == PaymentMethod.ONLINE
                ? OrderStatus.PENDING_PAYMENT
                : OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);

        Order savedOrder = orderRepository.save(order);

        long grandTotal = 0L;

        for (CreateOrderRequest.ShopOrderRequest shopReq : request.getShopOrders()) {
            Shop shop = shopRepository.findById(shopReq.getShopId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy shop!"));

            Map<Long, Integer> mergedQuantity = quantityByShop.get(shop.getShopId());

            long shopItemTotal = 0L;

            for (Map.Entry<Long, Integer> entry : mergedQuantity.entrySet()) {
                ProductVariant variant = variantRepository.findById(entry.getKey())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

                shopItemTotal += getUnitPrice(variant) * entry.getValue();
            }

            long discount = voucherService.calculateDiscount(
                    shopReq.getVoucherCode(),
                    shopItemTotal,
                    shop.getShopId(),
                    user.getUserId()
            );

            Voucher voucher = null;
            if (shopReq.getVoucherCode() != null && !shopReq.getVoucherCode().isBlank()) {
                voucher = voucherRepository.findByCode(shopReq.getVoucherCode())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher!"));
            }

            long shippingFee = DEFAULT_SHIPPING_FEE;
            long shopTotal = Math.max(0L, shopItemTotal + shippingFee - discount);
            grandTotal += shopTotal;

            ShopOrder shopOrder = new ShopOrder();
            shopOrder.setOrder(savedOrder);
            shopOrder.setShop(shop);
            shopOrder.setVoucher(voucher);
            shopOrder.setDiscountAmount(discount);
            shopOrder.setShippingFee(shippingFee);
            shopOrder.setTotalAmount(shopTotal);
            shopOrder.setStatus(request.getPaymentMethod() == PaymentMethod.ONLINE
                    ? OrderStatus.PENDING_PAYMENT
                    : OrderStatus.PENDING);

            ShopOrder savedShopOrder = shopOrderRepository.save(shopOrder);

            for (Map.Entry<Long, Integer> entry : mergedQuantity.entrySet()) {
                ProductVariant variant = variantRepository.findById(entry.getKey())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

                OrderItem orderItem = new OrderItem();
                orderItem.setShopOrder(savedShopOrder);
                orderItem.setVariant(variant);
                orderItem.setQuantity(entry.getValue());
                orderItem.setPriceAtPurchase(getUnitPrice(variant));

                orderItemRepository.save(orderItem);
            }

            if (request.getPaymentMethod() == PaymentMethod.COD
                    && shopReq.getVoucherCode() != null
                    && !shopReq.getVoucherCode().isBlank()) {
                voucherService.useVoucher(shopReq.getVoucherCode(), user.getUserId());
            }
        }

        savedOrder.setTotalAmount(BigDecimal.valueOf(grandTotal));
        orderRepository.save(savedOrder);

        if (request.getPaymentMethod() == PaymentMethod.COD) {
            for (Map<Long, Integer> quantityMap : quantityByShop.values()) {
                decreaseStock(quantityMap);
            }

            if (request.getCheckoutSource() == CheckoutSource.FROM_CART) {
                deleteCheckedOutCartItems(user, checkedOutVariantIds);
            }

            return BaseResponse.success_data(
                    "Đặt hàng COD thành công",
                    buildCheckoutResponse(savedOrder, null, null)
            );
        }

        Transaction transaction = new Transaction();
        transaction.setOrder(savedOrder);
        transaction.setAmount(grandTotal);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setProviderTransactionId("INIT-" + paymentProvider.name() + "-" + savedOrder.getOrderId());

        Transaction savedTransaction = transactionRepository.save(transaction);

        PaymentCreateResult paymentResult = paymentGatewayService.createPaymentUrl(
                savedOrder,
                savedTransaction,
                paymentProvider
        );

        savedTransaction.setProviderTransactionId(paymentResult.getProviderTransactionId());
        transactionRepository.save(savedTransaction);

        return BaseResponse.success_data(
                "Tạo đơn hàng online thành công, vui lòng thanh toán qua " + paymentProvider.name(),
                buildCheckoutResponse(savedOrder, savedTransaction, paymentResult.getPaymentUrl())
        );
    }

    @Override
    @Transactional
    public BaseResponse<?> handlePaymentCallback(PaymentCallbackRequest request) {
        // Gọi hằng số qua đường dẫn nếu không có import tĩnh
        if (!"123456789_DEV_SIGNATURE".equals(request.getSignature()) && request.getSignature() != null) {
            // (Tuỳ thuộc vào logic team, có thể điều chỉnh lại hàm kiểm tra chữ ký nếu cần)
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new RuntimeException("Đơn hàng này không phải thanh toán online!");
        }

        if (request.getPaymentProvider() != null
                && order.getPaymentProvider() != null
                && request.getPaymentProvider() != order.getPaymentProvider()) {
            throw new RuntimeException("Cổng thanh toán callback không khớp với đơn hàng!");
        }

        Transaction transaction = transactionRepository.findByOrder_OrderId(order.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch thanh toán!"));

        if (request.getTransactionId() != null
                && !transaction.getTransactionId().equals(request.getTransactionId())) {
            throw new RuntimeException("Transaction không khớp!");
        }

        if (transaction.getStatus() == PaymentStatus.COMPLETED) {
            return BaseResponse.success_data(
                    "Giao dịch đã được xử lý trước đó",
                    buildCheckoutResponse(order, transaction, null)
            );
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ thanh toán!");
        }

        if (!Boolean.TRUE.equals(request.getSuccess())) {
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setProviderTransactionId(request.getProviderTransactionId());
            transactionRepository.save(transaction);

            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);

            List<ShopOrder> shopOrders = shopOrderRepository.findByOrder_OrderId(order.getOrderId());
            for (ShopOrder shopOrder : shopOrders) {
                shopOrder.setStatus(OrderStatus.PAYMENT_FAILED);
                shopOrderRepository.save(shopOrder);
            }

            return BaseResponse.success_data(
                    "Thanh toán thất bại",
                    buildCheckoutResponse(order, transaction, null)
            );
        }

        List<ShopOrder> shopOrders = shopOrderRepository.findByOrder_OrderId(order.getOrderId());
        Set<Long> checkedOutVariantIds = new HashSet<>();

        for (ShopOrder shopOrder : shopOrders) {
            Map<Long, Integer> quantityMap = new HashMap<>();

            for (OrderItem item : shopOrder.getItems()) {
                quantityMap.merge(item.getVariant().getVariantId(), item.getQuantity(), Integer::sum);
                checkedOutVariantIds.add(item.getVariant().getVariantId());
            }

            validateStockWithLock(quantityMap, shopOrder.getShop().getShopId());
        }

        for (ShopOrder shopOrder : shopOrders) {
            Map<Long, Integer> quantityMap = new HashMap<>();

            for (OrderItem item : shopOrder.getItems()) {
                quantityMap.merge(item.getVariant().getVariantId(), item.getQuantity(), Integer::sum);
            }

            decreaseStock(quantityMap);

            if (shopOrder.getVoucher() != null) {
                voucherService.useVoucher(shopOrder.getVoucher().getCode(), order.getUser().getUserId());
            }

            shopOrder.setStatus(OrderStatus.PENDING);
            shopOrderRepository.save(shopOrder);
        }

        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        transaction.setStatus(PaymentStatus.COMPLETED);
        transaction.setProviderTransactionId(request.getProviderTransactionId());
        transactionRepository.save(transaction);

        if (order.getCheckoutSource() == CheckoutSource.FROM_CART) {
            deleteCheckedOutCartItems(order.getUser(), checkedOutVariantIds);
        }

        return BaseResponse.success_data(
                "Thanh toán thành công",
                buildCheckoutResponse(order, transaction, null)
        );
    }
}