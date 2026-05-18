package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity @Table(name = "Shops")
@Getter
@Setter
public class Shop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shopId;

    @Column(nullable = false, length = 150)
    private String shopName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pickupAddress;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private ShopStatus status;

    @OneToOne @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "shop")
    private List<Product> products;

    @OneToMany(mappedBy = "shop")
    private List<Voucher> vouchers;
}