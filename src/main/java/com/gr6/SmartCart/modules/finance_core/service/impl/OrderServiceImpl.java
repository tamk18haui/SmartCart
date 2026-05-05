package com.gr6.SmartCart.modules.finance_core.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.*;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.PaymentMethod;
import com.gr6.SmartCart.common.enums.PaymentStatus;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutPreviewRequest;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutPreviewResponse;
import com.gr6.SmartCart.modules.finance_core.dto.CreateOrderRequest;
import com.gr6.SmartCart.modules.finance_core.repository.*;
import com.gr6.SmartCart.modules.finance_core.service.OrderService;
import com.gr6.SmartCart.modules.finance_core.service.VoucherService;
import com.gr6.SmartCart.modules.identity.repository.ShopRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ShopOrderRepository shopOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShopRepository shopRepository;
    private final VoucherService voucherService;

    // 1. Lấy thông tin User đang đăng nhập
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Chưa đăng nhập hệ thống!"));
    }

    // 2. Xử lý địa chỉ
    private Address resolveAddress(Long requestAddressId, User user) {
        if (requestAddressId != null) {
            Address address = addressRepository.findById(requestAddressId)
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại!"));
            if (!address.getUser().getUserId().equals(user.getUserId()) || address.getIsDeleted()) {
                throw new RuntimeException("Địa chỉ không hợp lệ hoặc đã bị xóa!");
            }
            return address;
        } else {
            List<Address> userAddresses = addressRepository.findByUser(user).stream()
                    .filter(a -> !a.getIsDeleted())
                    .toList();

            if (userAddresses.isEmpty()) return null;

            return userAddresses.stream()
                    .filter(Address::getIsDefault)
                    .findFirst()
                    .orElse(userAddresses.get(0));
        }
    }

    // =================================================================
    // API 1: HIỂN THỊ MÀN HÌNH THANH TOÁN (Preview)
    // =================================================================
    @Override
    public BaseResponse<CheckoutPreviewResponse> getCheckoutPreview(CheckoutPreviewRequest request) {
        User user = getCurrentUser();
        Address selectedAddress = resolveAddress(request.getAddressId(), user);

        long grandTotalItemPrice = 0L;
        long grandTotalAmount = 0L;
        long grandTotalDiscount = 0L;
        long totalShippingFee = 0L;

        List<CheckoutPreviewResponse.ShopPreview> shopPreviews = new ArrayList<>();

        for (CheckoutPreviewRequest.ShopOrderPreviewDto shopReq : request.getShopOrders()) {
            Shop shop = shopRepository.findById(shopReq.getShopId())
                    .orElseThrow(() -> new RuntimeException("Shop không tồn tại!"));

            long shopSubtotal = 0L;
            List<CheckoutPreviewResponse.ItemPreview> itemPreviews = new ArrayList<>();

            for (CheckoutPreviewRequest.ItemPreviewDto itemReq : shopReq.getItems()) {
                ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

                String optionValuesStr = "";
                if (variant.getVariantOptionValues() != null) {
                    optionValuesStr = variant.getVariantOptionValues().stream()
                            .map(v -> v.getOptionValue().getValue())
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                }

                long price = variant.getPrice().longValue();
                shopSubtotal += price * itemReq.getQuantity();

                itemPreviews.add(CheckoutPreviewResponse.ItemPreview.builder()
                        .productName(variant.getProduct().getName())
                        .variantImageUrl(variant.getImageUrl() != null ? variant.getImageUrl() : variant.getProduct().getImageUrls())
                        .price(price)
                        .quantity(itemReq.getQuantity())
                        .optionValues(optionValuesStr)
                        .build());
            }

            long discount = voucherService.calculateDiscount(shopReq.getVoucherCode(), shopSubtotal, shop.getShopId(), user.getUserId());
            long shippingFee = 30000L; // TODO: Đẩy con số này ra file config cấu hình sau
            long finalShopTotal = Math.max(0, shopSubtotal + shippingFee - discount);

            grandTotalItemPrice += shopSubtotal;
            totalShippingFee += shippingFee;
            grandTotalAmount += finalShopTotal;
            grandTotalDiscount += discount;

            shopPreviews.add(CheckoutPreviewResponse.ShopPreview.builder()
                    .shopName(shop.getShopName())
                    .items(itemPreviews)
                    .shopItemTotal(shopSubtotal)
                    .shopShippingFee(shippingFee)
                    .shopDiscount(discount)
                    .subtotal(finalShopTotal)
                    .build());
        }

        CheckoutPreviewResponse.AddressPreviewDto addressDto = null;
        if (selectedAddress != null) {
            addressDto = CheckoutPreviewResponse.AddressPreviewDto.builder()
                    .addressId(selectedAddress.getAddressId())
                    .receiverName(selectedAddress.getReceiverName())
                    .receiverPhone(selectedAddress.getReceiverPhone())
                    .fullAddress(selectedAddress.getFullAddress())
                    .build();
        }

        CheckoutPreviewResponse response = CheckoutPreviewResponse.builder()
                .defaultAddress(addressDto)
                .shops(shopPreviews)
                .totalShippingFee(totalShippingFee)
                .totalAmount(grandTotalAmount)
                .totalItemPrice(grandTotalItemPrice)
                .totalDiscount(grandTotalDiscount)
                .build();

        return BaseResponse.success_data("Lấy dữ liệu màn hình thanh toán thành công", response);
    }

    // =================================================================
    // API 2: CHỐT ĐƠN VÀ TRỪ KHO (Checkout)
    // =================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<?> createOrder(CreateOrderRequest request) {
        User user = getCurrentUser();

        if (request.getAddressId() == null) {
            return BaseResponse.error(400, "Vui lòng thêm địa chỉ giao hàng trước khi đặt hàng!");
        }
        Address address = resolveAddress(request.getAddressId(), user);

        // BƯỚC 1: TÍNH TOÁN NHANH TIỀN BẠC TRƯỚC KHI TẠO ĐƠN
        Map<Long, Long> shopSubtotalMap = new HashMap<>();
        Map<Long, Long> discountMap = new HashMap<>();

        for (CreateOrderRequest.ShopOrderDto shopDto : request.getShopOrders()) {
            long subtotal = 0L;

            for (CreateOrderRequest.OrderItemDto item : shopDto.getItems()) {
                // Chỉ đọc thông thường để tính tiền
                ProductVariant variant = variantRepository.findById(item.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

                if (!variant.getProduct().getShop().getShopId().equals(shopDto.getShopId())) {
                    throw new RuntimeException("Sản phẩm không thuộc cửa hàng này!");
                }

                subtotal += variant.getPrice().longValue() * item.getQuantity();
            }

            long discount = voucherService.calculateDiscount(shopDto.getVoucherCode(), subtotal, shopDto.getShopId(), user.getUserId());
            shopSubtotalMap.put(shopDto.getShopId(), subtotal);
            discountMap.put(shopDto.getShopId(), discount);
        }

        // BƯỚC 2: TẠO ĐƠN HÀNG GỐC
        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setShippingAddress(address.getFullAddress());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTotalAmount(BigDecimal.ZERO);
        order = orderRepository.save(order);

        BigDecimal grandTotal = BigDecimal.ZERO;

        // BƯỚC 3: TRỪ TỒN KHO AN TOÀN VÀ LƯU CHI TIẾT
        for (CreateOrderRequest.ShopOrderDto shopDto : request.getShopOrders()) {
            Shop shop = shopRepository.findById(shopDto.getShopId())
                    .orElseThrow(() -> new RuntimeException("Cửa hàng không tồn tại!"));

            ShopOrder shopOrder = new ShopOrder();
            shopOrder.setOrder(order);
            shopOrder.setShop(shop);
            shopOrder.setShippingFee(30000L);

            long subtotal = shopSubtotalMap.get(shopDto.getShopId());
            long discount = discountMap.get(shopDto.getShopId());

            List<OrderItem> items = new ArrayList<>();

            for (CreateOrderRequest.OrderItemDto item : shopDto.getItems()) {
                // SỬA LỖI CHÍ MẠNG: Khóa Row ngay lúc này để giành quyền trừ kho độc quyền
                ProductVariant variant = variantRepository.findByIdWithLock(item.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Lỗi dữ liệu: Sản phẩm đã bị xóa hoặc ẩn khỏi hệ thống!"));

                // Kiểm tra Tồn kho LẠI MỘT LẦN NỮA sau khi đã có khóa
                if (variant.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Sản phẩm '" + variant.getProduct().getName() + "' đã hết hàng hoặc không đủ số lượng!");
                }

                // Trừ tồn kho an toàn
                variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
                variantRepository.save(variant);

                OrderItem oi = new OrderItem();
                oi.setShopOrder(shopOrder);
                oi.setVariant(variant);
                oi.setQuantity(item.getQuantity());
                oi.setPriceAtPurchase(variant.getPrice().longValue());
                items.add(oi);
            }

            long finalTotal = subtotal + shopOrder.getShippingFee() - discount;
            shopOrder.setDiscountAmount(discount);
            shopOrder.setTotalAmount(Math.max(0, finalTotal));

            shopOrderRepository.save(shopOrder);
            orderItemRepository.saveAll(items);

            // Chốt sử dụng Voucher
            voucherService.useVoucher(shopDto.getVoucherCode(), user.getUserId());
            grandTotal = grandTotal.add(BigDecimal.valueOf(finalTotal));
        }

        order.setTotalAmount(grandTotal);
        orderRepository.save(order);

        // BƯỚC 4: XỬ LÝ PHƯƠNG THỨC THANH TOÁN
        if (request.getPaymentMethod() == PaymentMethod.ONLINE) {
            String momoPaymentUrl = "https://test-payment.momo.vn/v2/gateway/api/create?orderId="
                    + order.getOrderId() + "&amount=" + grandTotal.longValue();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("orderId", order.getOrderId());
            responseData.put("redirectUrl", momoPaymentUrl);

            return BaseResponse.success_data("Chuyển hướng đến cổng thanh toán", responseData);
        }

        return BaseResponse.success_data("Đặt hàng thành công", order.getOrderId());
    }
}