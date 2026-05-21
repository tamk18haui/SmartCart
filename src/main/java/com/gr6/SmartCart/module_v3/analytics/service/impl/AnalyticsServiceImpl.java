package com.gr6.SmartCart.module_v3.analytics.service.impl;

import com.gr6.SmartCart.common.exception.CustomException;
import com.gr6.SmartCart.module_v3.analytics.dto.request.DateFilterRequest;
import com.gr6.SmartCart.module_v3.analytics.dto.response.DailyRevenueDTO;
import com.gr6.SmartCart.module_v3.analytics.dto.response.RevenueReportResponse;
import com.gr6.SmartCart.module_v3.analytics.dto.response.TopProductDTO;
import com.gr6.SmartCart.module_v3.analytics.dto.response.TransactionStatResponse;
import com.gr6.SmartCart.module_v3.analytics.repository.projections.DailyRevenueProjection;
import com.gr6.SmartCart.module_v3.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final com.gr6.SmartCart.module_v3.analytics.repository.AnalyticsRepository analyticsRepository;

    private void validateDate(DateFilterRequest filter) {
        if (!filter.isValidDateRange()) {
            throw new CustomException(400, "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc!");
        }
    }

    @Override
    public List<TopProductDTO> getTopSellingProducts(Long shopId, DateFilterRequest filter, int limit) {
        validateDate(filter);
        return analyticsRepository.getTopSellingProducts(shopId, filter.getStartDate(), filter.getEndDate(), limit)
                .stream()
                .map(p -> TopProductDTO.builder()
                        .productId(p.getProductId())
                        .name(p.getName())
                        .imageUrl(p.getImageUrl())
                        .basePrice(p.getBasePrice())
                        .totalSold(p.getTotalSold())
                        .totalRevenue(p.getTotalRevenue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public RevenueReportResponse getRevenueReport(Long shopId, DateFilterRequest filter) {
        validateDate(filter);

        List<DailyRevenueProjection> projections;
        if (shopId == null) {
            projections = analyticsRepository.getAdminDailyRevenue(filter.getStartDate(), filter.getEndDate());
        } else {
            projections = analyticsRepository.getShopDailyRevenue(shopId, filter.getStartDate(), filter.getEndDate());
        }

        long totalRevenue = 0L;
        long totalOrders = 0L;
        List<DailyRevenueDTO> dailyDetails = projections.stream().map(p -> {
            DailyRevenueDTO dto = new DailyRevenueDTO(p.getReportDate(), p.getRevenue(), p.getOrderCount());
            return dto;
        }).collect(Collectors.toList());

        for (DailyRevenueDTO d : dailyDetails) {
            totalRevenue += d.getRevenue();
            totalOrders += d.getOrderCount();
        }

        return RevenueReportResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .dailyDetails(dailyDetails)
                .build();
    }

    @Override
    public TransactionStatResponse getTransactionStats(DateFilterRequest filter) {
        validateDate(filter);
        return TransactionStatResponse.builder()
                .totalTransactions(analyticsRepository.countTotalTransactions(filter.getStartDate(), filter.getEndDate()))
                .successfulTransactions(analyticsRepository.countSuccessfulTransactions(filter.getStartDate(), filter.getEndDate()))
                .failedTransactions(analyticsRepository.countFailedTransactions(filter.getStartDate(), filter.getEndDate()))
                .totalVolume(analyticsRepository.sumTotalVolume(filter.getStartDate(), filter.getEndDate()))
                .build();
    }
}