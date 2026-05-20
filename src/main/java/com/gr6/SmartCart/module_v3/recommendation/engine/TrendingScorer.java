package com.gr6.SmartCart.module_v3.recommendation.engine;

import com.gr6.SmartCart.common.domain.Product;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thuật toán tính điểm Trending (sản phẩm HOT).
 *
 * Công thức:
 *   TrendingScore = (lượt mua gần đây × 5) + (lượt thêm giỏ × 3) + (soldCount × 0.5) + (avgRating × reviewCount × 2)
 *
 * Trọng số được thiết kế để ưu tiên: mua > giỏ hàng > đánh giá > lượt bán tổng
 */
@Component
public class TrendingScorer {

    private static final double PURCHASE_WEIGHT = 5.0;
    private static final double CART_WEIGHT = 3.0;
    private static final double SOLD_WEIGHT = 0.5;
    private static final double RATING_WEIGHT = 2.0;

    /**
     * Tính điểm trending cho danh sách sản phẩm.
     *
     * @param purchaseCounts  Map<productId, số lượng mua gần đây>
     * @param cartCounts      Map<productId, số lượng trong giỏ hàng>
     * @param ratings         Map<productId, [avgRating, reviewCount]>
     * @param products        danh sách sản phẩm active
     * @return Map<productId, trendingScore>
     */
    public Map<Long, Double> score(
            Map<Long, Long> purchaseCounts,
            Map<Long, Long> cartCounts,
            Map<Long, double[]> ratings,
            List<Product> products) {

        Map<Long, Double> scores = new HashMap<>();

        for (Product p : products) {
            Long id = p.getProductId();

            // Lượt mua gần đây (tín hiệu mạnh nhất)
            double purchaseScore = purchaseCounts.getOrDefault(id, 0L) * PURCHASE_WEIGHT;

            // Số lượng đang nằm trong giỏ hàng (tín hiệu quan tâm)
            double cartScore = cartCounts.getOrDefault(id, 0L) * CART_WEIGHT;

            // Tổng số đã bán (tín hiệu lịch sử)
            int soldCount = p.getSoldCount() != null ? p.getSoldCount() : 0;
            double soldScore = soldCount * SOLD_WEIGHT;

            // Đánh giá: avgRating × reviewCount (sản phẩm 5 sao với 10 reviews > 5 sao với 1 review)
            double ratingScore = 0;
            if (ratings.containsKey(id)) {
                double[] r = ratings.get(id);
                double avgRating = r[0];
                double reviewCount = r[1];
                ratingScore = avgRating * reviewCount * RATING_WEIGHT;
            }

            double totalScore = purchaseScore + cartScore + soldScore + ratingScore;
            scores.put(id, totalScore);
        }

        return scores;
    }
}
