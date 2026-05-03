package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.ProductCondition;
import com.gr6.SmartCart.common.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Products")
@Data
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(length = 100)
    private String brand;

    @Enumerated(EnumType.STRING) @Column(name = "product_condition", length = 50)
    private ProductCondition condition;

    @Column(precision = 18, scale = 2)
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal length;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal width;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal height;

    private Boolean isPreOrder;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private ProductStatus status;

    @ManyToOne @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductOption> options;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductVariant> variants;
}