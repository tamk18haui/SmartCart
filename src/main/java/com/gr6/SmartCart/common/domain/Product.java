package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.ProductCondition;
import com.gr6.SmartCart.common.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "Products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String brand;

    @Column(columnDefinition = "int default 0")
    private Integer soldCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", nullable = false, length = 50)
    private ProductCondition condition;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(precision = 10, scale = 2)
    private BigDecimal length;

    @Column(precision = 10, scale = 2)
    private BigDecimal width;

    @Column(precision = 10, scale = 2)
    private BigDecimal height;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // LƯU LỊCH SỬ ADMIN KHÓA
    @Column(columnDefinition = "TEXT")
    private String banReason;

    private LocalDateTime bannedAt;

    private String bannedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ProductStatus previousStatus;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductOption> options;
}