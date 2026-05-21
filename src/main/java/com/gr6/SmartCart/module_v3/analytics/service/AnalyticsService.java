package com.gr6.SmartCart.module_v3.analytics.service;

import com.gr6.SmartCart.module_v3.analytics.dto.request.DateFilterRequest;
import com.gr6.SmartCart.module_v3.analytics.dto.response.RevenueReportResponse;
import com.gr6.SmartCart.module_v3.analytics.dto.response.TopProductDTO;
import com.gr6.SmartCart.module_v3.analytics.dto.response.TransactionStatResponse;

import java.util.List;

public interface AnalyticsService {
    List<TopProductDTO> getTopSellingProducts(Long shopId, DateFilterRequest filter, int limit);
    RevenueReportResponse getRevenueReport(Long shopId, DateFilterRequest filter); // shopId null = Admin
    TransactionStatResponse getTransactionStats(DateFilterRequest filter);
}