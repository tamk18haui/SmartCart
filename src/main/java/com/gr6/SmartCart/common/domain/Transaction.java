package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "Transactions")
@Getter
@Setter
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @OneToOne @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(length = 100)
    private String providerTransactionId;

    private Long amount;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private PaymentStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;
}