package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.WalletTransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "Wallet_Transactions")
@Getter
@Setter
public class WalletTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletTxId;

    @ManyToOne @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private WalletTransactionType type;

    private Long amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;
}