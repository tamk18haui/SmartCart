package com.gr6.SmartCart.modules.finance_core.dto;

import com.gr6.SmartCart.common.enums.CheckoutSource;
import com.gr6.SmartCart.common.enums.PaymentMethod;
import com.gr6.SmartCart.common.enums.PaymentProvider;

import java.util.List;

public class CreateOrderRequest {
    private Long addressId;
    private PaymentMethod paymentMethod;
    private PaymentProvider paymentProvider;
    private CheckoutSource checkoutSource;
    private List<ShopOrderRequest> shopOrders;

    // --- GETTERS & SETTERS BỌC THÉP ---
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentProvider getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(PaymentProvider paymentProvider) { this.paymentProvider = paymentProvider; }

    public CheckoutSource getCheckoutSource() { return checkoutSource; }
    public void setCheckoutSource(CheckoutSource checkoutSource) { this.checkoutSource = checkoutSource; }

    public List<ShopOrderRequest> getShopOrders() { return shopOrders; }
    public void setShopOrders(List<ShopOrderRequest> shopOrders) { this.shopOrders = shopOrders; }

    // --- INNER CLASS: ShopOrderRequest ---
    public static class ShopOrderRequest {
        private Long shopId;
        private String voucherCode;
        private List<ItemRequest> items;

        public Long getShopId() { return shopId; }
        public void setShopId(Long shopId) { this.shopId = shopId; }

        public String getVoucherCode() { return voucherCode; }
        public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

        public List<ItemRequest> getItems() { return items; }
        public void setItems(List<ItemRequest> items) { this.items = items; }
    }

    // --- INNER CLASS: ItemRequest ---
    public static class ItemRequest {
        private Long variantId;
        private Integer quantity;

        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}