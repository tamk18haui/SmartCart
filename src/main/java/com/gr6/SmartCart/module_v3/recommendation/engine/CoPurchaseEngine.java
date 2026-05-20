package com.gr6.SmartCart.module_v3.recommendation.engine;

import com.gr6.SmartCart.common.domain.Product;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thuật toán gợi ý dựa trên hành vi mua hàng (Collaborative Filtering đơn giản).
 *
 * Kết hợp 3 nguồn dữ liệu:
 *   1. Co-Purchase: Sản phẩm hay được mua kèm (từ hóa đơn người khác) → trọng số cao nhất
 *   2. Similar Name: Biến thể tương tự từ shop khác (tên giống) → trọng số trung bình
 *   3. Same Category: Cùng danh mục nhưng khác shop → trọng số thấp hơn
 */
@Component
public class CoPurchaseEngine {

    private static final double CO_PURCHASE_WEIGHT = 5.0;
    private static final double SIMILAR_NAME_WEIGHT = 3.0;
    private static final double SAME_CATEGORY_WEIGHT = 2.0;

    /**
     * Gộp và tính điểm từ 3 nguồn gợi ý.
     *
     * @param coPurchaseData      kết quả query co-purchase: [[productId, orderCount], ...]
     * @param sameCategoryProducts sản phẩm cùng danh mục khác shop
     * @param similarNameProducts  sản phẩm có tên tương tự khác shop
     * @return Map<productId, combinedScore>
     */
    public Map<Long, Double> score(
            List<Object[]> coPurchaseData,
            List<Product> sameCategoryProducts,
            List<Product> similarNameProducts) {

        Map<Long, Double> scores = new HashMap<>();

        // 1. Co-Purchase: sản phẩm xuất hiện cùng trong đơn hàng → tín hiệu mạnh nhất
        for (Object[] row : coPurchaseData) {
            Long productId = (Long) row[0];
            Long orderCount = (Long) row[1];
            scores.merge(productId, orderCount * CO_PURCHASE_WEIGHT, Double::sum);
        }

        // 2. Similar Name: biến thể từ shop khác (ví dụ: "Áo thun Nike" ở shop A → gợi ý "Áo thun Nike" ở shop B)
        for (Product p : similarNameProducts) {
            scores.merge(p.getProductId(), SIMILAR_NAME_WEIGHT, Double::sum);
        }

        // 3. Same Category: cùng danh mục nhưng khác shop (thay thế trực tiếp)
        for (Product p : sameCategoryProducts) {
            scores.merge(p.getProductId(), SAME_CATEGORY_WEIGHT, Double::sum);
        }

        return scores;
    }
}
