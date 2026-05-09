package com.gr6.SmartCart.modules.storefront.dto;

public class CartItemRequest {
    private Long variantId;
    private Integer quantity;
    
    // Getters and Setters
    public Long getVariantId() { return this.variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}