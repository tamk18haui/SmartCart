package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.PaymentMethod;
import com.gr6.SmartCart.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name = "Orders")
@Data
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private OrderStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<ShopOrder> shopOrders;

    @OneToOne(mappedBy = "order")
    private Transaction transaction;

    @OneToOne(mappedBy = "order")
    private Review review;
}
