package com.gr6.SmartCart.module_v3.analytics.repository.projections;

public interface TopProductProjection {
    Long getProductId();
    String getName();
    String getImageUrl();
    Long getBasePrice();
    Long getTotalSold();
    Long getTotalRevenue();
}