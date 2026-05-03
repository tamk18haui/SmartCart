package com.gr6.SmartCart.common.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity @Table(name = "Product_Option_Values")
@Data
public class ProductOptionValue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionValueId;

    @ManyToOne @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;

    @Column(nullable = false, length = 100)
    private String value;
}