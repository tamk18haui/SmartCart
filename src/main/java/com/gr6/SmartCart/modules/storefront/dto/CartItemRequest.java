package com.gr6.SmartCart.modules.storefront.dto;

public class CartItemRequest {
    private Long productId;
    private Integer quantity;
    
    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}