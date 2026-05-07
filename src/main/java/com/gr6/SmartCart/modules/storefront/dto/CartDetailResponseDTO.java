package com.gr6.SmartCart.modules.storefront.dto;
import java.util.List;

import lombok.Data;

@Data
public class CartDetailResponseDTO {
    private List<ShopCartDTO> shops; // Danh sách các Shop
    private Integer totalItems;      // Tổng số lượng sản phẩm
    private Double totalPrice;       // Tổng tiền cần thanh toán
    private Boolean isEmpty;
}