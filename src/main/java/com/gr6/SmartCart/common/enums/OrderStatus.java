package com.gr6.SmartCart.common.enums;

public enum OrderStatus {
    PENDING_PAYMENT, // Chờ thanh toán online
    PENDING,         // Chờ xác nhận
    CONFIRMED,       // Đã xác nhận
    PREPARING,       // Đang chuẩn bị hàng (ĐÃ THÊM THEO CHUẨN E-COMMERCE)
    SHIPPING,        // Đang giao hàng
    DELIVERED,       // Đã giao hàng
    COMPLETED,       // Hoàn thành
    CANCELLED,       // Đã hủy
    PAYMENT_FAILED   // Thanh toán thất bại
}