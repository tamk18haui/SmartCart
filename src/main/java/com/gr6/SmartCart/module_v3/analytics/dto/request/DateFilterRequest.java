package com.gr6.SmartCart.module_v3.analytics.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DateFilterRequest {
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    // Tự động kiểm tra hợp lệ khi gọi API
    @JsonIgnore
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) return false;
        return !startDate.isAfter(endDate); // startDate <= endDate
    }
}