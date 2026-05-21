package com.gr6.SmartCart.module_v3.recommendation.service;

import com.gr6.SmartCart.module_v3.recommendation.dto.RecommendedProductDTO;

import java.util.List;
import java.util.Map;

public interface RecommendationService {

    /**
     * Sản phẩm đang HOT (trending) trong N ngày gần đây — có phân trang cho infinite scroll.
     *
     * @param days số ngày (7, 14 hoặc 30)
     * @param page trang hiện tại (bắt đầu từ 0)
     * @param size số sản phẩm mỗi trang
     * @return Map chứa products + thông tin phân trang
     */
    Map<String, Object> getTrendingProducts(int days, int page, int size);

    /**
     * Gợi ý sản phẩm liên quan đến từ khóa tìm kiếm — có phân trang.
     *
     * @param keyword từ khóa tìm kiếm
     * @param page trang hiện tại
     * @param size số sản phẩm mỗi trang
     * @return Map chứa products + thông tin phân trang
     */
    Map<String, Object> getSearchBasedRecommendations(String keyword, int page, int size);

    /**
     * Gợi ý sản phẩm dựa trên 1 sản phẩm cụ thể (khi user xem/thêm giỏ/mua) — có phân trang.
     *
     * @param productId sản phẩm gốc
     * @param page trang hiện tại
     * @param size số sản phẩm mỗi trang
     * @return Map chứa products + thông tin phân trang
     */
    Map<String, Object> getProductBasedRecommendations(Long productId, int page, int size);

    /**
     * Gợi ý cá nhân hóa dựa trên giỏ hàng + lịch sử mua của user — có phân trang.
     *
     * @param userId ID người dùng
     * @param page trang hiện tại
     * @param size số sản phẩm mỗi trang
     * @return Map chứa products + thông tin phân trang
     */
    Map<String, Object> getPersonalRecommendations(Long userId, int page, int size);
}
