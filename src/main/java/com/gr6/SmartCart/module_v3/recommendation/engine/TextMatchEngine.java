package com.gr6.SmartCart.module_v3.recommendation.engine;

import com.gr6.SmartCart.common.domain.Product;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thuật toán so khớp từ khóa tìm kiếm với sản phẩm.
 *
 * Cách hoạt động:
 *   1. Tách từ khóa thành các token (split theo khoảng trắng)
 *   2. Với mỗi sản phẩm, kiểm tra token có xuất hiện trong: tên, thương hiệu, danh mục, mô tả
 *   3. Mỗi vị trí match có trọng số khác nhau (tên > thương hiệu > danh mục > mô tả)
 *   4. Bonus nếu toàn bộ keyword xuất hiện nguyên vẹn trong tên
 */
@Component
public class TextMatchEngine {

    private static final double NAME_EXACT_BONUS = 15.0;
    private static final double NAME_TOKEN_WEIGHT = 10.0;
    private static final double BRAND_TOKEN_WEIGHT = 7.0;
    private static final double CATEGORY_TOKEN_WEIGHT = 5.0;
    private static final double DESC_TOKEN_WEIGHT = 2.0;

    /**
     * Tính điểm liên quan giữa keyword và danh sách sản phẩm.
     *
     * @param keyword  từ khóa tìm kiếm
     * @param products danh sách sản phẩm
     * @return Map<productId, textScore> (chỉ chứa sản phẩm có score > 0)
     */
    public Map<Long, Double> score(String keyword, List<Product> products) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyMap();
        }

        String normalizedKeyword = keyword.toLowerCase().trim();
        String[] tokens = normalizedKeyword.split("\\s+");

        Map<Long, Double> scores = new HashMap<>();

        for (Product p : products) {
            String name = safe(p.getName()).toLowerCase();
            String brand = safe(p.getBrand()).toLowerCase();
            String category = safe(p.getCategory().getCategoryName()).toLowerCase();
            String description = safe(p.getDescription()).toLowerCase();

            double score = 0;

            // So khớp từng token
            for (String token : tokens) {
                if (token.length() < 2) continue; // bỏ qua token quá ngắn

                if (name.contains(token)) score += NAME_TOKEN_WEIGHT;
                if (brand.contains(token)) score += BRAND_TOKEN_WEIGHT;
                if (category.contains(token)) score += CATEGORY_TOKEN_WEIGHT;
                if (description.contains(token)) score += DESC_TOKEN_WEIGHT;
            }

            // Bonus: toàn bộ keyword xuất hiện nguyên vẹn trong tên
            if (name.contains(normalizedKeyword)) {
                score += NAME_EXACT_BONUS;
            }

            if (score > 0) {
                scores.put(p.getProductId(), score);
            }
        }

        return scores;
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
