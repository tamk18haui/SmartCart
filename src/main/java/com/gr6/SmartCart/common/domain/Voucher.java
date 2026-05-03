package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.DiscountType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity @Table(name = "Vouchers")
@Data
public class Voucher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voucherId;

    @ManyToOne @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(length = 50, unique = true)
    private String code;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private DiscountType discountType;

    private Long discountValue;
    private Long minOrderValue;
    private Long maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(length = 50)
    private String status;
}