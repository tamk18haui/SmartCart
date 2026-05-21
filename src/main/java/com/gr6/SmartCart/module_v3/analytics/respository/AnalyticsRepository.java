package com.gr6.SmartCart.module_v3.analytics.repository;

import com.gr6.SmartCart.module_v3.analytics.repository.projections.DailyRevenueProjection;
import com.gr6.SmartCart.module_v3.analytics.repository.projections.TopProductProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<com.gr6.SmartCart.common.domain.Order, Long> {

    // 1. Thống kê sản phẩm bán chạy
    // Đã sửa: Liên kết qua bảng product_variants, lấy ảnh từ p.image_urls, lấy ngày từ bảng orders
    @Query(value = "SELECT p.product_id AS productId, p.name AS name, p.base_price AS basePrice, " +
            "p.image_urls AS imageUrl, " +
            "SUM(oi.quantity) AS totalSold, SUM(oi.quantity * oi.price_at_purchase) AS totalRevenue " +
            "FROM order_items oi " +
            "JOIN product_variants pv ON oi.variant_id = pv.variant_id " +
            "JOIN products p ON pv.product_id = p.product_id " +
            "JOIN shop_orders so ON oi.shop_order_id = so.shop_order_id " +
            "JOIN orders o ON so.order_id = o.order_id " +
            "WHERE so.status = 'COMPLETED' " +
            "AND (:shopId IS NULL OR so.shop_id = :shopId) " +
            "AND DATE(o.created_at) BETWEEN :startDate AND :endDate " +
            "GROUP BY p.product_id, p.name, p.base_price, p.image_urls " +
            "ORDER BY totalSold DESC LIMIT :limit", nativeQuery = true)
    List<TopProductProjection> getTopSellingProducts(@Param("shopId") Long shopId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     @Param("limit") int limit);

    // 2. Thống kê doanh thu theo ngày cho ADMIN (Bảng Orders)
    @Query(value = "SELECT DATE(o.created_at) AS reportDate, SUM(o.total_amount) AS revenue, COUNT(o.order_id) AS orderCount " +
            "FROM orders o " +
            "WHERE o.status = 'COMPLETED' AND DATE(o.created_at) BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(o.created_at) " +
            "ORDER BY DATE(o.created_at) ASC", nativeQuery = true)
    List<DailyRevenueProjection> getAdminDailyRevenue(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 3. Thống kê doanh thu theo ngày cho SHOP
    // Đã sửa: JOIN bảng orders để lấy cột created_at
    @Query(value = "SELECT DATE(o.created_at) AS reportDate, SUM(so.total_amount) AS revenue, COUNT(so.shop_order_id) AS orderCount " +
            "FROM shop_orders so " +
            "JOIN orders o ON so.order_id = o.order_id " +
            "WHERE so.shop_id = :shopId AND so.status = 'COMPLETED' AND DATE(o.created_at) BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(o.created_at) " +
            "ORDER BY DATE(o.created_at) ASC", nativeQuery = true)
    List<DailyRevenueProjection> getShopDailyRevenue(@Param("shopId") Long shopId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 4. Các hàm thống kê Transaction (Bảng Transactions)
    @Query(value = "SELECT COUNT(transaction_id) FROM transactions WHERE DATE(created_at) BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countTotalTransactions(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT COUNT(transaction_id) FROM transactions WHERE status = 'COMPLETED' AND DATE(created_at) BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countSuccessfulTransactions(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT COUNT(transaction_id) FROM transactions WHERE status = 'FAILED' AND DATE(created_at) BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countFailedTransactions(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE status = 'COMPLETED' AND DATE(created_at) BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long sumTotalVolume(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}