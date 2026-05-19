package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.ShopOrder;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "Seller_Settlements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seller_settlement_shop_order",
                        columnNames = {"shop_order_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_order_id", nullable = false, unique = true)
    private ShopOrder shopOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false)
    private Long grossAmount;

    @Column(nullable = false)
    private Long commissionAmount;

    @Column(nullable = false)
    private Long netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SettlementStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(length = 100)
    private String settledBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}