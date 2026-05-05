package com.gr6.SmartCart.modules.finance_core.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CheckoutPreviewResponse {
    private AddressPreviewDto defaultAddress;

    private List<ShopPreview> shops;
    // --- Các thông số cho Bill tổng ---
    private Long totalItemPrice;    // Tổng tiền hàng gốc (tất cả các shop)
    private Long totalShippingFee;  // Tổng phí ship
    private Long totalDiscount;     // Tổng tiền được giảm
    private Long totalAmount;       // Tiền khách phải trả cuối cùng

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
        private Long shopId;
        private String shopName;
        private List<ItemPreview> items;
        private Long shopItemTotal;      // Tiền hàng của riêng shop này
        private Long shopShippingFee;    // Phí ship của riêng shop này
        private Long shopDiscount;       // Số tiền voucher của shop này
        private Long subtotal;           // Tổng tiền shop này (hàng + ship - voucher)
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