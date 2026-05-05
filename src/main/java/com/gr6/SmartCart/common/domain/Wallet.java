package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name = "Wallets")
@Getter
@Setter
public class Wallet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @OneToOne @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Long balance;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private WalletStatus status;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<WalletTransaction> walletTransactions;
}