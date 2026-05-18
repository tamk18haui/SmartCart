package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.CategoryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table (name = "Categories")
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false, unique = true)
    private String categoryName;

    @Column (columnDefinition = "TEXT")
    private String categoryDescription;

    @Column(columnDefinition = "TEXT")
    private String categoryImageUrl;
    @Enumerated(EnumType.STRING)
    private CategoryStatus categoryStatus;

    @OneToMany(mappedBy = "category")
    private List<Product> products;

}
