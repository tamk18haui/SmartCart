package com.gr6.SmartCart.module_v3.analytics.repository.projections;

public interface DailyRevenueProjection {
    String getReportDate();
    Long getRevenue();
    Long getOrderCount();
}