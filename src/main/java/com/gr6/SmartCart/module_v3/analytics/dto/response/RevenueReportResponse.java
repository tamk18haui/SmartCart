package com.gr6.SmartCart.module_v3.analytics.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RevenueReportResponse {
    private Long totalRevenue;
    private Long totalOrders;
    private List<DailyRevenueDTO> dailyDetails; // Dữ liệu cho Chart
}