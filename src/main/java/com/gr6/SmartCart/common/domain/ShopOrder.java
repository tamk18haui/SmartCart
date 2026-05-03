package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity @Table(name = "Shop_Orders")
@Data
public class ShopOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shopOrderId;

    @ManyToOne @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    private Long discountAmount;
    private Long shippingFee;
    private Long totalAmount;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private OrderStatus status;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @OneToMany(mappedBy = "shopOrder", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @ManyToOne @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;
}