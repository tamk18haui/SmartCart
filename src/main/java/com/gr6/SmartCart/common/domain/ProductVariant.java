package com.gr6.SmartCart.common.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity @Table(name = "Product_Variants")
@Getter
@Setter
public class ProductVariant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long variantId;

    @ManyToOne @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 100)
    private String sku;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL)
    private List<VariantOptionValue> variantOptionValues;
}