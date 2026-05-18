package com.gr6.SmartCart.modules.storefront.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SearchFilterRequest {
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}