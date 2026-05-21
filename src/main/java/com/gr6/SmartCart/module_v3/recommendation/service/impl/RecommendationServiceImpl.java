package com.gr6.SmartCart.module_v3.recommendation.service.impl;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.module_v3.recommendation.dto.RecommendedProductDTO;
import com.gr6.SmartCart.module_v3.recommendation.engine.CoPurchaseEngine;
import com.gr6.SmartCart.module_v3.recommendation.engine.TextMatchEngine;
import com.gr6.SmartCart.module_v3.recommendation.engine.TrendingScorer;
import com.gr6.SmartCart.module_v3.recommendation.repository.RecommendationRepository;
import com.gr6.SmartCart.module_v3.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository repository;
    private final TrendingScorer trendingScorer;
    private final TextMatchEngine textMatchEngine;
    private final CoPurchaseEngine coPurchaseEngine;

    private static final List<OrderStatus> COMPLETED_STATUSES =
            List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED);

    // ========================
    // 1. TRENDING (SẢN PHẨM HOT) — INFINITE SCROLL
    // ========================

    @Override
    public Map<String, Object> getTrendingProducts(int days, int page, int size) {
        if (days != 7 && days != 14 && days != 30) days = 7;

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        // Lấy TẤT CẢ sản phẩm active
        List<Product> products = repository.findAllActiveProducts(ProductStatus.ACTIVE);
        if (products.isEmpty()) return emptyPage(page, size);

        // Thu thập dữ liệu scoring
        Map<Long, Long> purchaseCounts = toMapLong(
                repository.findPurchaseCountsSince(COMPLETED_STATUSES, since, ProductStatus.ACTIVE));
        Map<Long, Long> cartCounts = toMapLong(
                repository.findCartCountsByProduct(ProductStatus.ACTIVE));
        Map<Long, double[]> ratings = toRatingsMap(repository.findRatingsByProduct());
        Map<Long, BigDecimal[]> prices = toPriceMap(
                repository.findVariantPriceRanges(ProductStatus.ACTIVE));

        // Tính điểm trending cho TẤT CẢ sản phẩm
        Map<Long, Double> scores = trendingScorer.score(purchaseCounts, cartCounts, ratings, products);

        // Sắp xếp theo score giảm dần
        List<Product> sorted = products.stream()
                .sorted((a, b) -> Double.compare(
                        scores.getOrDefault(b.getProductId(), 0.0),
                        scores.getOrDefault(a.getProductId(), 0.0)))
                .toList();

        // Phân trang
        String reason = "🔥 Hot trong " + days + " ngày";
        return paginate(sorted, scores, reason, ratings, prices, page, size);
    }

    // ========================
    // 2. GỢI Ý THEO TỪ KHÓA — INFINITE SCROLL
    // ========================

    @Override
    public Map<String, Object> getSearchBasedRecommendations(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) return emptyPage(page, size);

        List<Product> products = repository.findAllActiveProducts(ProductStatus.ACTIVE);
        if (products.isEmpty()) return emptyPage(page, size);

        Map<Long, double[]> ratings = toRatingsMap(repository.findRatingsByProduct());
        Map<Long, BigDecimal[]> prices = toPriceMap(
                repository.findVariantPriceRanges(ProductStatus.ACTIVE));

        // Tính điểm text match
        Map<Long, Double> scores = textMatchEngine.score(keyword, products);

        // Chỉ lấy sản phẩm có score > 0, sắp xếp giảm dần
        List<Product> sorted = products.stream()
                .filter(p -> scores.containsKey(p.getProductId()))
                .sorted((a, b) -> Double.compare(
                        scores.getOrDefault(b.getProductId(), 0.0),
                        scores.getOrDefault(a.getProductId(), 0.0)))
                .toList();

        String reason = "🔍 Liên quan: \"" + keyword + "\"";
        return paginate(sorted, scores, reason, ratings, prices, page, size);
    }

    // ========================
    // 3. GỢI Ý THEO SẢN PHẨM (HÀNH VI) — INFINITE SCROLL
    // ========================

    @Override
    public Map<String, Object> getProductBasedRecommendations(Long productId, int page, int size) {
        Optional<Product> optProduct = repository.findById(productId);
        if (optProduct.isEmpty()) return emptyPage(page, size);

        Product product = optProduct.get();
        Long categoryId = product.getCategory().getCategoryId();
        Long shopId = product.getShop().getShopId();
        String nameKeyword = extractKeyword(product.getName());

        // 3 nguồn gợi ý
        List<Object[]> coPurchaseData = repository.findCoPurchasedProductIds(
                productId, COMPLETED_STATUSES, ProductStatus.ACTIVE);
        List<Product> sameCategoryProducts = repository.findSameCategoryDifferentShop(
                categoryId, shopId, productId, ProductStatus.ACTIVE);
        List<Product> similarNameProducts = nameKeyword.isBlank()
                ? List.of()
                : repository.findByNameContainingDifferentShop(
                        nameKeyword, shopId, productId, ProductStatus.ACTIVE);

        // Gộp điểm từ 3 nguồn
        Map<Long, Double> scores = coPurchaseEngine.score(
                coPurchaseData, sameCategoryProducts, similarNameProducts);

        if (scores.isEmpty()) return emptyPage(page, size);

        // Lấy thông tin đánh giá và giá
        Map<Long, double[]> ratings = toRatingsMap(repository.findRatingsByProduct());
        Map<Long, BigDecimal[]> prices = toPriceMap(
                repository.findVariantPriceRanges(ProductStatus.ACTIVE));

        // Thu thập tất cả sản phẩm gợi ý
        Map<Long, Product> productMap = buildProductMap(sameCategoryProducts, similarNameProducts, scores.keySet());

        // Sắp xếp theo score giảm dần
        List<Product> sorted = scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(e -> productMap.get(e.getKey()))
                .filter(Objects::nonNull)
                .toList();

        // Xây dựng reason cho từng sản phẩm
        Map<Long, String> reasons = new HashMap<>();
        for (Product p : sorted) {
            reasons.put(p.getProductId(),
                    buildReason(p.getProductId(), coPurchaseData, sameCategoryProducts, similarNameProducts));
        }

        return paginateWithReasons(sorted, scores, reasons, ratings, prices, page, size);
    }

    // ========================
    // 4. GỢI Ý CÁ NHÂN HÓA — INFINITE SCROLL
    // ========================

    @Override
    public Map<String, Object> getPersonalRecommendations(Long userId, int page, int size) {
        List<Long> cartProductIds = repository.findProductIdsInUserCart(userId);
        List<Long> orderProductIds = repository.findRecentOrderProductIds(userId, COMPLETED_STATUSES);

        Set<Long> interactedProductIds = new LinkedHashSet<>();
        interactedProductIds.addAll(cartProductIds);
        interactedProductIds.addAll(orderProductIds);

        if (interactedProductIds.isEmpty()) {
            return getTrendingProducts(7, page, size);
        }

        // Chạy product-based recommendation cho từng sản phẩm đã tương tác
        Map<Long, Double> aggregatedScores = new HashMap<>();

        for (Long productId : interactedProductIds) {
            Optional<Product> optProduct = repository.findById(productId);
            if (optProduct.isEmpty()) continue;

            Product product = optProduct.get();
            Long categoryId = product.getCategory().getCategoryId();
            Long shopId = product.getShop().getShopId();
            String nameKeyword = extractKeyword(product.getName());

            List<Object[]> coPurchaseData = repository.findCoPurchasedProductIds(
                    productId, COMPLETED_STATUSES, ProductStatus.ACTIVE);
            List<Product> sameCategoryProducts = repository.findSameCategoryDifferentShop(
                    categoryId, shopId, productId, ProductStatus.ACTIVE);
            List<Product> similarNameProducts = nameKeyword.isBlank()
                    ? List.of()
                    : repository.findByNameContainingDifferentShop(
                            nameKeyword, shopId, productId, ProductStatus.ACTIVE);

            Map<Long, Double> partialScores = coPurchaseEngine.score(
                    coPurchaseData, sameCategoryProducts, similarNameProducts);

            partialScores.forEach((pid, score) ->
                    aggregatedScores.merge(pid, score, Double::sum));
        }

        // Loại bỏ sản phẩm user đã tương tác
        interactedProductIds.forEach(aggregatedScores::remove);

        if (aggregatedScores.isEmpty()) {
            return getTrendingProducts(7, page, size);
        }

        // Load thông tin
        Map<Long, double[]> ratings = toRatingsMap(repository.findRatingsByProduct());
        Map<Long, BigDecimal[]> prices = toPriceMap(
                repository.findVariantPriceRanges(ProductStatus.ACTIVE));
        List<Product> allActive = repository.findAllActiveProducts(ProductStatus.ACTIVE);
        Map<Long, Product> productMap = allActive.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));

        List<Product> sorted = aggregatedScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(e -> productMap.get(e.getKey()))
                .filter(Objects::nonNull)
                .toList();

        String reason = "✨ Dành riêng cho bạn";
        return paginate(sorted, aggregatedScores, reason, ratings, prices, page, size);
    }

    // ========================
    // PAGINATION HELPERS
    // ========================

    /**
     * Cắt danh sách theo page/size và trả về kèm thông tin phân trang.
     * Giống Shopee: app gọi page=0 lấy 10 sản phẩm đầu, lướt xuống gọi page=1 lấy 10 tiếp theo...
     */
    private Map<String, Object> paginate(List<Product> sorted, Map<Long, Double> scores,
                                          String reason, Map<Long, double[]> ratings,
                                          Map<Long, BigDecimal[]> prices, int page, int size) {
        int totalElements = sorted.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;

        if (fromIndex >= totalElements) {
            return buildPageResponse(List.of(), page, size, totalElements, totalPages, false);
        }

        int toIndex = Math.min(fromIndex + size, totalElements);
        List<Product> pageContent = sorted.subList(fromIndex, toIndex);
        boolean hasMore = toIndex < totalElements;

        List<RecommendedProductDTO> dtos = pageContent.stream()
                .map(p -> mapToDTO(p,
                        scores.getOrDefault(p.getProductId(), 0.0),
                        reason, ratings, prices))
                .toList();

        return buildPageResponse(dtos, page, size, totalElements, totalPages, hasMore);
    }

    /**
     * Phân trang với reason riêng cho từng sản phẩm (dùng cho product-based)
     */
    private Map<String, Object> paginateWithReasons(List<Product> sorted, Map<Long, Double> scores,
                                                     Map<Long, String> reasons,
                                                     Map<Long, double[]> ratings,
                                                     Map<Long, BigDecimal[]> prices, int page, int size) {
        int totalElements = sorted.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;

        if (fromIndex >= totalElements) {
            return buildPageResponse(List.of(), page, size, totalElements, totalPages, false);
        }

        int toIndex = Math.min(fromIndex + size, totalElements);
        List<Product> pageContent = sorted.subList(fromIndex, toIndex);
        boolean hasMore = toIndex < totalElements;

        List<RecommendedProductDTO> dtos = pageContent.stream()
                .map(p -> mapToDTO(p,
                        scores.getOrDefault(p.getProductId(), 0.0),
                        reasons.getOrDefault(p.getProductId(), "💡 Có thể bạn thích"),
                        ratings, prices))
                .toList();

        return buildPageResponse(dtos, page, size, totalElements, totalPages, hasMore);
    }

    /**
     * Build response JSON cho phân trang
     */
    private Map<String, Object> buildPageResponse(List<RecommendedProductDTO> products,
                                                    int page, int size,
                                                    int totalElements, int totalPages,
                                                    boolean hasMore) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("products", products);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("hasMore", hasMore);
        return response;
    }

    /**
     * Trả về trang rỗng
     */
    private Map<String, Object> emptyPage(int page, int size) {
        return buildPageResponse(List.of(), page, size, 0, 0, false);
    }

    // ========================
    // DATA HELPERS
    // ========================

    private Map<Long, Long> toMapLong(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            Long count = ((Number) row[1]).longValue();
            map.put(productId, count);
        }
        return map;
    }

    private Map<Long, double[]> toRatingsMap(List<Object[]> rows) {
        Map<Long, double[]> map = new HashMap<>();
        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            double avgRating = ((Number) row[1]).doubleValue();
            double reviewCount = ((Number) row[2]).doubleValue();
            map.put(productId, new double[]{avgRating, reviewCount});
        }
        return map;
    }

    private Map<Long, BigDecimal[]> toPriceMap(List<Object[]> rows) {
        Map<Long, BigDecimal[]> map = new HashMap<>();
        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            BigDecimal minPrice = (BigDecimal) row[1];
            BigDecimal maxPrice = (BigDecimal) row[2];
            map.put(productId, new BigDecimal[]{minPrice, maxPrice});
        }
        return map;
    }

    private RecommendedProductDTO mapToDTO(Product p, Double score, String reason,
                                           Map<Long, double[]> ratings,
                                           Map<Long, BigDecimal[]> prices) {
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        if (prices.containsKey(p.getProductId())) {
            BigDecimal[] priceRange = prices.get(p.getProductId());
            minPrice = priceRange[0];
            maxPrice = priceRange[1];
        }

        Double avgRating = null;
        Long reviewCount = null;
        if (ratings.containsKey(p.getProductId())) {
            double[] r = ratings.get(p.getProductId());
            avgRating = Math.round(r[0] * 10.0) / 10.0;
            reviewCount = (long) r[1];
        }

        return RecommendedProductDTO.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .imageUrl(extractFirstImage(p.getImageUrls()))
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .shopName(p.getShop().getShopName())
                .shopId(p.getShop().getShopId())
                .categoryName(p.getCategory().getCategoryName())
                .soldCount(p.getSoldCount() != null ? p.getSoldCount() : 0)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .score(Math.round(score * 100.0) / 100.0)
                .reason(reason)
                .build();
    }

    private String extractFirstImage(String imageUrls) {
        if (imageUrls == null || imageUrls.isBlank()) return null;
        return imageUrls.split(",")[0].trim();
    }

    private String extractKeyword(String productName) {
        if (productName == null || productName.isBlank()) return "";
        String[] words = productName.trim().split("\\s+");
        if (words.length >= 2) return words[0] + " " + words[1];
        return words[0];
    }

    private Map<Long, Product> buildProductMap(List<Product> sameCat, List<Product> similarName,
                                                Set<Long> allIds) {
        Map<Long, Product> map = new HashMap<>();
        for (Product p : sameCat) map.put(p.getProductId(), p);
        for (Product p : similarName) map.putIfAbsent(p.getProductId(), p);

        Set<Long> missing = allIds.stream()
                .filter(id -> !map.containsKey(id))
                .collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            for (Product p : repository.findAllById(missing)) {
                map.put(p.getProductId(), p);
            }
        }
        return map;
    }

    private String buildReason(Long productId, List<Object[]> coPurchaseData,
                               List<Product> sameCatProducts, List<Product> similarNameProducts) {
        for (Object[] row : coPurchaseData) {
            if (row[0].equals(productId)) return "🛒 Người khác cũng mua";
        }
        for (Product p : similarNameProducts) {
            if (p.getProductId().equals(productId)) return "🔄 Sản phẩm tương tự";
        }
        for (Product p : sameCatProducts) {
            if (p.getProductId().equals(productId)) return "📦 Cùng danh mục";
        }
        return "💡 Có thể bạn thích";
    }
}
