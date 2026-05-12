package com.gr6.SmartCart.modules.finance_core.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Address;
import com.gr6.SmartCart.common.domain.Order;
import com.gr6.SmartCart.common.domain.OrderItem;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.ShopOrder;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.PaymentMethod;
import com.gr6.SmartCart.common.enums.PaymentStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutPreviewRequest;
import com.gr6.SmartCart.modules.finance_core.dto.CheckoutPreviewResponse;
import com.gr6.SmartCart.modules.finance_core.dto.CreateOrderRequest;
import com.gr6.SmartCart.modules.finance_core.repository.AddressRepository;
import com.gr6.SmartCart.modules.finance_core.repository.OrderItemRepository;
import com.gr6.SmartCart.modules.finance_core.repository.OrderRepository;
import com.gr6.SmartCart.modules.finance_core.repository.ShopOrderRepository;
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
import java.util.stream.Collectors;

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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Chưa đăng nhập hệ thống!"));
    }

    private Address resolveAddress(Long requestAddressId, User user) {
        if (requestAddressId != null) {
            Address address = addressRepository.findById(requestAddressId)
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại!"));
            if (!address.getUser().getUserId().equals(user.getUserId()) || Boolean.TRUE.equals(address.getIsDeleted())) {
                throw new RuntimeException("Địa chỉ không hợp lệ hoặc đã bị xóa!");
            }
            return address;
        }

        List<Address> userAddresses = addressRepository.findByUser(user).stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .toList();

        if (userAddresses.isEmpty()) {
            return null;
        }

        return userAddresses.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .findFirst()
                .orElse(userAddresses.get(0));
    }

    private void validatePurchasableVariant(ProductVariant variant) {
        if (variant == null || variant.getStatus() != VariantStatus.ACTIVE) {
            throw new RuntimeException("Phân loại sản phẩm hiện không khả dụng!");
        }

        Product product = variant.getProduct();
        if (product == null || product.getStatus() != ProductStatus.ACTIVE) {
            throw new RuntimeException("Sản phẩm hiện không khả dụng!");
        }

        if (product.getCategory() == null || product.getCategory().getCategoryStatus() != CategoryStatus.ACTIVE) {
            throw new RuntimeException("Danh mục sản phẩm hiện không khả dụng!");
        }

        Shop shop = product.getShop();
        if (shop == null || shop.getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop hiện không khả dụng!");
        }
    }

    private long getUnitPrice(ProductVariant variant) {
        if (variant.getPrice() != null) {
            return variant.getPrice().longValue();
        }
        if (variant.getProduct() != null && variant.getProduct().getBasePrice() != null) {
            return variant.getProduct().getBasePrice().longValue();
        }
        throw new RuntimeException("Giá sản phẩm không hợp lệ!");
    }

    private String buildOptionValues(ProductVariant variant) {
        if (variant.getVariantOptionValues() == null || variant.getVariantOptionValues().isEmpty()) {
            return "";
        }
        return variant.getVariantOptionValues().stream()
                .filter(vov -> vov.getOptionValue() != null)
                .map(vov -> vov.getOptionValue().getValue())
                .collect(Collectors.joining(", "));
    }

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
            if (shop.getStatus() != ShopStatus.ACTIVE) {
                throw new RuntimeException("Shop hiện không khả dụng!");
            }

            long shopSubtotal = 0L;
            List<CheckoutPreviewResponse.ItemPreview> itemPreviews = new ArrayList<>();

            for (CheckoutPreviewRequest.ItemPreviewDto itemReq : shopReq.getItems()) {
                ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy phân loại sản phẩm!"));

                validatePurchasableVariant(variant);

                if (!variant.getProduct().getShop().getShopId().equals(shopReq.getShopId())) {
                    throw new RuntimeException("Sản phẩm không thuộc shop này!");
                }
                if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                    throw new RuntimeException("Số lượng mua phải lớn hơn 0!");
                }
                if (variant.getStockQuantity() == null || variant.getStockQuantity() < itemReq.getQuantity()) {
                    throw new RuntimeException("Sản phẩm không đủ tồn kho!");
                }

                long price = getUnitPrice(variant);
                shopSubtotal += price * itemReq.getQuantity();

                itemPreviews.add(CheckoutPreviewResponse.ItemPreview.builder()
                        .productName(variant.getProduct().getName())
                        .variantImageUrl(variant.getImageUrl() != null ? variant.getImageUrl() : variant.getProduct().getImageUrls())
                        .price(price)
                        .quantity(itemReq.getQuantity())
                        .optionValues(buildOptionValues(variant))
                        .build());
            }

            long discount = voucherService.calculateDiscount(shopReq.getVoucherCode(), shopSubtotal, shop.getShopId(), user.getUserId());
            long shippingFee = 30000L;
            long finalShopTotal = Math.max(0, shopSubtotal + shippingFee - discount);

            grandTotalItemPrice += shopSubtotal;
            totalShippingFee += shippingFee;
            grandTotalAmount += finalShopTotal;
            grandTotalDiscount += discount;

            shopPreviews.add(CheckoutPreviewResponse.ShopPreview.builder()
                    .shopId(shop.getShopId())
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<?> createOrder(CreateOrderRequest request) {
        User user = getCurrentUser();
        if (request.getAddressId() == null) {
            return BaseResponse.error(400, "Vui lòng thêm địa chỉ giao hàng trước khi đặt hàng!");
        }
        Address address = resolveAddress(request.getAddressId(), user);

        Map<Long, Long> shopSubtotalMap = new HashMap<>();
        Map<Long, Long> discountMap = new HashMap<>();

        for (CreateOrderRequest.ShopOrderDto shopDto : request.getShopOrders()) {
            Shop shop = shopRepository.findById(shopDto.getShopId())
                    .orElseThrow(() -> new RuntimeException("Shop không tồn tại!"));
            if (shop.getStatus() != ShopStatus.ACTIVE) {
                throw new RuntimeException("Shop hiện không khả dụng!");
            }

            long subtotal = 0L;
            for (CreateOrderRequest.OrderItemDto item : shopDto.getItems()) {
                ProductVariant variant = variantRepository.findByIdWithLock(item.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy phân loại sản phẩm!"));

                validatePurchasableVariant(variant);

                if (!variant.getProduct().getShop().getShopId().equals(shopDto.getShopId())) {
                    throw new RuntimeException("Sản phẩm không thuộc cửa hàng này!");
                }
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    throw new RuntimeException("Số lượng mua phải lớn hơn 0!");
                }
                if (variant.getStockQuantity() == null || variant.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Sản phẩm không đủ tồn kho!");
                }

                subtotal += getUnitPrice(variant) * item.getQuantity();
            }

            long discount = voucherService.calculateDiscount(shopDto.getVoucherCode(), subtotal, shopDto.getShopId(), user.getUserId());
            shopSubtotalMap.put(shopDto.getShopId(), subtotal);
            discountMap.put(shopDto.getShopId(), discount);
        }

        long totalShippingFee = 30000L * request.getShopOrders().size();
        long totalDiscount = discountMap.values().stream().mapToLong(Long::longValue).sum();
        long totalItemPrice = shopSubtotalMap.values().stream().mapToLong(Long::longValue).sum();
        long grandTotal = Math.max(0, totalItemPrice + totalShippingFee - totalDiscount);

        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setShippingAddress(address.getFullAddress());
        order.setTotalAmount(BigDecimal.valueOf(grandTotal));
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);

        for (CreateOrderRequest.ShopOrderDto shopDto : request.getShopOrders()) {
            Shop shop = shopRepository.findById(shopDto.getShopId())
                    .orElseThrow(() -> new RuntimeException("Shop không tồn tại!"));

            long shippingFee = 30000L;
            long subtotal = shopSubtotalMap.get(shopDto.getShopId());
            long discount = discountMap.get(shopDto.getShopId());

            ShopOrder shopOrder = new ShopOrder();
            shopOrder.setOrder(order);
            shopOrder.setShop(shop);
            shopOrder.setShippingFee(shippingFee);
            shopOrder.setDiscountAmount(discount);
            shopOrder.setTotalAmount(Math.max(0, subtotal + shippingFee - discount));
            shopOrder.setStatus(OrderStatus.PENDING);
            shopOrder = shopOrderRepository.save(shopOrder);

            for (CreateOrderRequest.OrderItemDto item : shopDto.getItems()) {
                ProductVariant variant = variantRepository.findByIdWithLock(item.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy phân loại sản phẩm!"));

                // KIỂM TRA LẠI SAU KHI LẤY LOCK
                validatePurchasableVariant(variant);

                if (!variant.getProduct().getShop().getShopId().equals(shopDto.getShopId())) {
                    throw new RuntimeException("Sản phẩm không thuộc cửa hàng này!");
                }
                if (variant.getStockQuantity() == null || variant.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Sản phẩm không đủ tồn kho!");
                }

                variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
                variantRepository.save(variant);

                OrderItem orderItem = new OrderItem();
                orderItem.setShopOrder(shopOrder);
                orderItem.setVariant(variant);
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPriceAtPurchase(getUnitPrice(variant));
                orderItemRepository.save(orderItem);
            }

            if (shopDto.getVoucherCode() != null && !shopDto.getVoucherCode().isBlank()) {
                voucherService.useVoucher(shopDto.getVoucherCode(), user.getUserId());
            }
        }

        if (request.getPaymentMethod() == PaymentMethod.ONLINE) {
            String momoPaymentUrl = "https://test-payment.momo.vn/v2/gateway/api/create?orderId="
                    + order.getOrderId() + "&amount=" + grandTotal;
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("orderId", order.getOrderId());
            responseData.put("redirectUrl", momoPaymentUrl);

            return BaseResponse.success_data("Chuyển hướng đến cổng thanh toán", responseData);
        }

        return BaseResponse.success_data("Đặt hàng thành công", order.getOrderId());
    }
}