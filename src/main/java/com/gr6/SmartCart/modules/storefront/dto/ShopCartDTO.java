package com.gr6.SmartCart.modules.storefront.dto;
import java.util.List;

import lombok.Data;

@Data
public class ShopCartDTO {
    private Long shopId;
    private String shopName;
    private List<CartItemResponseDTO> items;
    private Double shopSubtotal; // Tiền tạm tính của shop này

    // Explicit setters and getters
    public Long getShopId() {
        return shopId;
    }
    
    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }
    
    public String getShopName() {
        return shopName;
    }
    
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    
    public List<CartItemResponseDTO> getItems() {
        return items;
    }
    
    public void setItems(List<CartItemResponseDTO> items) {
        this.items = items;
    }
    
    public Double getShopSubtotal() {
        return shopSubtotal;
    }
    
    public void setShopSubtotal(Double shopSubtotal) {
        this.shopSubtotal = shopSubtotal;
    }
}