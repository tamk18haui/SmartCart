package com.gr6.SmartCart.module_v3.recommendation.controller;

import com.gr6.SmartCart.module_v3.recommendation.repository.RecommendationRepository;
import com.gr6.SmartCart.module_v3.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v3/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "API gợi ý sản phẩm thông minh — hỗ trợ infinite scroll")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final RecommendationRepository recommendationRepository;

    /**
     * Sản phẩm đang HOT (trending) — INFINITE SCROLL.
     * App gọi: page=0 → hiện 10 sản phẩm đầu, lướt xuống → page=1 → 10 sản phẩm tiếp...
     * Khi hasMore=false → hết sản phẩm.
     */
    @GetMapping("/trending")
    @Operation(summary = "Trending (infinite scroll)",
               description = "Trả về sản phẩm HOT có phân trang. Dùng page/size để cuộn vô hạn như Shopee.")
    public ResponseEntity<Map<String, Object>> getTrending(
            @Parameter(description = "Số ngày (7, 14, 30)") @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "Trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số sản phẩm mỗi trang") @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> result = recommendationService.getTrendingProducts(days, page, size);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("strategy", "trending");
        response.put("days", days);
        response.putAll(result);
        return ResponseEntity.ok(response);
    }

    /**
     * Gợi ý sản phẩm theo từ khóa tìm kiếm — INFINITE SCROLL.
     */
    @GetMapping("/search")
    @Operation(summary = "Gợi ý theo từ khóa (infinite scroll)",
               description = "Trả về sản phẩm liên quan keyword có phân trang")
    public ResponseEntity<Map<String, Object>> getSearchBased(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> result = recommendationService.getSearchBasedRecommendations(keyword, page, size);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("strategy", "search");
        response.put("keyword", keyword);
        response.putAll(result);
        return ResponseEntity.ok(response);
    }

    /**
     * Gợi ý liên quan đến 1 sản phẩm cụ thể — INFINITE SCROLL.
     * Gọi khi user xem chi tiết / thêm giỏ / mua hàng.
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Gợi ý theo sản phẩm (infinite scroll)",
               description = "Trả về sản phẩm liên quan: mua kèm, cùng danh mục, biến thể khác shop")
    public ResponseEntity<Map<String, Object>> getProductBased(
            @Parameter(description = "ID sản phẩm gốc") @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> result = recommendationService.getProductBasedRecommendations(productId, page, size);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("strategy", "product_based");
        response.put("sourceProductId", productId);
        response.putAll(result);
        return ResponseEntity.ok(response);
    }

    /**
     * Gợi ý cá nhân hóa — INFINITE SCROLL.
     * Yêu cầu đăng nhập (JWT token).
     */
    @GetMapping("/personal")
    @Operation(summary = "Gợi ý cá nhân hóa (infinite scroll)",
               description = "Sản phẩm dành riêng cho user dựa trên giỏ hàng + lịch sử mua")
    public ResponseEntity<Map<String, Object>> getPersonal(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String email = authentication.getName();
        Long userId = recommendationRepository.findUserIdByEmail(email);

        if (userId == null) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("strategy", "personal");
            response.put("products", List.of());
            response.put("page", page);
            response.put("size", size);
            response.put("totalElements", 0);
            response.put("totalPages", 0);
            response.put("hasMore", false);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> result = recommendationService.getPersonalRecommendations(userId, page, size);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("strategy", "personal");
        response.putAll(result);
        return ResponseEntity.ok(response);
    }
}
