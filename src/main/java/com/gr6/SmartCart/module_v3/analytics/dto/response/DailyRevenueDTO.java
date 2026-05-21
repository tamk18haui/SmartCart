package com.gr6.SmartCart.module_v3.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyRevenueDTO {
    private String date; // Format YYYY-MM-DD
    private Long revenue;
    private Long orderCount;
}