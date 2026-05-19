package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.domain.Wallet;
import com.gr6.SmartCart.common.enums.WithdrawStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Withdraw_Requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long withdrawId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 120)
    private String bankName;

    @Column(nullable = false, length = 50)
    private String bankAccountNumber;

    @Column(nullable = false, length = 120)
    private String bankAccountHolder;

    @Column(columnDefinition = "TEXT")
    private String sellerNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WithdrawStatus status;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @Column(length = 100)
    private String transferCode;

    @Column(length = 100)
    private String processedBy;

    private LocalDateTime processedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}