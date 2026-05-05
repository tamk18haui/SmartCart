package com.gr6.SmartCart.modules.finance_core.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CheckoutPreviewResponse {
    private AddressPreviewDto defaultAddress;

    private List<ShopPreview> shops;
    private Long totalShippingFee;
    private Long totalAmount;

    @Data
    @Builder
    public static class AddressPreviewDto {
        private Long addressId;
        private String receiverName;
        private String receiverPhone;
        private String fullAddress;
    }

    @Data
    @Builder
    public static class ShopPreview {
        private String shopName;
        private List<ItemPreview> items;
        private Long subtotal;
    }

    @Data
    @Builder
    public static class ItemPreview {
        private String productName;
        private String variantImageUrl;
        private Long price;
        private Integer quantity;
        private String optionValues;
    }
}