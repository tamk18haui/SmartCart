package com.gr6.SmartCart.modules.storefront.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SearchFilterRequest {

    private String keyword;

    private Long categoryId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    /*
     * relevance  : liên quan nhất
     * newest     : mới nhất
     * sold_desc  : bán chạy
     * price_asc  : giá thấp đến cao
     * price_desc : giá cao đến thấp
     */
    private String sortBy;
}