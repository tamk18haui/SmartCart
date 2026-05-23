package com.gr6.SmartCart.modules.fulfillment.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Order;
import com.gr6.SmartCart.common.domain.OrderItem;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.ShopOrder;
import com.gr6.SmartCart.modules.finance_core.repository.ShopOrderRepository;
import com.gr6.SmartCart.modules.fulfillment.dto.OrderDetailResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.OrderItemDTO;
import com.gr6.SmartCart.modules.fulfillment.dto.OrderListResponse;
import com.gr6.SmartCart.modules.fulfillment.service.OrderQueryService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final ShopOrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<OrderListResponse>> getAllOrders(String keyword) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ShopOrder> orders = orderRepository.searchOrdersByShop(email, keyword);

        if (orders.isEmpty()) {
            return BaseResponse.success_data("Chưa có đơn hàng nào!", List.of());
        }

        List<OrderListResponse> response = orders.stream()
                .map(this::toOrderListResponse)
                .collect(Collectors.toList());

        return BaseResponse.success(response);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<OrderDetailResponse> getOrderDetail(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        ShopOrder shopOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (shopOrder.getShop() == null
                || shopOrder.getShop().getUser() == null
                || !email.equals(shopOrder.getShop().getUser().getEmail())) {
            return BaseResponse.error(404, "Bạn không có quyền xem đơn hàng này");
        }

        Order parentOrder = shopOrder.getOrder();

        OrderDetailResponse detail = OrderDetailResponse.builder()
                .id(shopOrder.getShopOrderId())
                .orderCode(buildOrderCode(shopOrder))
                .status(shopOrder.getStatus())
                .createdAt(parentOrder == null ? null : parentOrder.getCreatedAt())
                .totalAmount(toBigDecimal(shopOrder.getTotalAmount()))
                .subtotalAmount(calculateSubtotal(shopOrder.getItems()))
                .shippingFee(toBigDecimal(shopOrder.getShippingFee()))
                .buyerId(parentOrder == null || parentOrder.getUser() == null ? null : parentOrder.getUser().getUserId())
                .buyerName(parentOrder == null || parentOrder.getUser() == null ? null : parentOrder.getUser().getFullName())
                .receiverName(parentOrder == null ? null : parentOrder.getReceiverName())
                .receiverPhone(parentOrder == null ? null : parentOrder.getReceiverPhone())
                .shippingAddress(parentOrder == null ? null : parentOrder.getShippingAddress())
                .items(toOrderItemDTOs(shopOrder.getItems()))
                .build();

        return BaseResponse.success(detail);
    }

    private OrderListResponse toOrderListResponse(ShopOrder shopOrder) {
        Order parentOrder = shopOrder.getOrder();
        OrderItem firstItem = getFirstItem(shopOrder);

        return OrderListResponse.builder()
                .id(shopOrder.getShopOrderId())
                .orderCode(buildOrderCode(shopOrder))
                .totalAmount(toBigDecimal(shopOrder.getTotalAmount()))
                .status(shopOrder.getStatus())
                .createdAt(parentOrder == null ? null : parentOrder.getCreatedAt())
                .customerName(parentOrder == null ? null : parentOrder.getReceiverName())
                .receiverPhone(parentOrder == null ? null : parentOrder.getReceiverPhone())
                .firstProductName(resolveProductName(firstItem))
                .firstVariantName(resolveVariantName(firstItem))
                .firstProductImage(resolveItemImageUrl(firstItem))
                .build();
    }

    private List<OrderItemDTO> toOrderItemDTOs(List<OrderItem> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(item -> OrderItemDTO.builder()
                        .productName(resolveProductName(item))
                        .variantName(resolveVariantName(item))
                        .quantity(item.getQuantity() == null ? 0 : item.getQuantity())
                        .price(toBigDecimal(item.getPriceAtPurchase()))
                        .imageUrl(resolveItemImageUrl(item))
                        .build()
                )
                .collect(Collectors.toList());
    }

    private OrderItem getFirstItem(ShopOrder shopOrder) {
        if (shopOrder == null || shopOrder.getItems() == null || shopOrder.getItems().isEmpty()) {
            return null;
        }

        return shopOrder.getItems().get(0);
    }

    private String buildOrderCode(ShopOrder shopOrder) {
        if (shopOrder == null || shopOrder.getShopOrderId() == null) {
            return "ORD";
        }

        return "ORD-" + shopOrder.getShopOrderId();
    }

    private String resolveProductName(OrderItem item) {
        if (item == null || item.getVariant() == null || item.getVariant().getProduct() == null) {
            return "Sản phẩm";
        }

        String name = item.getVariant().getProduct().getName();

        if (name == null || name.isBlank()) {
            return "Sản phẩm";
        }

        return name;
    }

    private String resolveVariantName(OrderItem item) {
        if (item == null || item.getVariant() == null) {
            return "";
        }

        String sku = item.getVariant().getSku();

        if (sku == null || sku.isBlank()) {
            return "";
        }

        return sku;
    }

    private String resolveItemImageUrl(OrderItem item) {
        if (item == null || item.getVariant() == null) {
            return null;
        }

        ProductVariant variant = item.getVariant();

        if (variant.getImageUrl() != null && !variant.getImageUrl().isBlank()) {
            return variant.getImageUrl().trim();
        }

        Product product = variant.getProduct();

        if (product == null) {
            return null;
        }

        return firstImageFromProductImages(product.getImageUrls());
    }

    private String firstImageFromProductImages(String imageUrls) {
        if (imageUrls == null || imageUrls.isBlank()) {
            return null;
        }

        String value = imageUrls.trim();

        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        String[] parts = value.split(",");

        if (parts.length == 0) {
            return null;
        }

        String first = parts[0]
                .trim()
                .replace("\"", "")
                .replace("'", "");

        return first.isBlank() ? null : first;
    }

    private BigDecimal calculateSubtotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItem item : items) {
            if (item == null) continue;

            BigDecimal price = toBigDecimal(item.getPriceAtPurchase());
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity() == null ? 0 : item.getQuantity());

            subtotal = subtotal.add(price.multiply(quantity));
        }

        return subtotal;
    }

    private BigDecimal toBigDecimal(Long value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(value);
    }
}