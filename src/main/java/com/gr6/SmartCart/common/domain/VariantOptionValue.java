package com.gr6.SmartCart.common.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity @Table(name = "Variant_Option_Values")
@Data
public class VariantOptionValue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne @JoinColumn(name = "option_value_id", nullable = false)
    private ProductOptionValue optionValue;
}