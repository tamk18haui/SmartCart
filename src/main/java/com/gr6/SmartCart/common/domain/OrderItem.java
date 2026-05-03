package com.gr6.SmartCart.common.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity @Table(name = "Order_Items")
@Data
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne @JoinColumn(name = "shop_order_id", nullable = false)
    private ShopOrder shopOrder;

    @ManyToOne @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private Integer quantity;

    private Long priceAtPurchase;
}