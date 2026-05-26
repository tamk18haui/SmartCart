package com.gr6.SmartCart.module_v3.recommendation.ai;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.UserProductEvent;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.common.enums.RecommendationEventType;
import com.gr6.SmartCart.module_v3.recommendation.ai.dto.AiProductCandidate;
import com.gr6.SmartCart.module_v3.recommendation.ai.dto.AiRecommendItem;
import com.gr6.SmartCart.module_v3.recommendation.ai.dto.AiRecommendResponse;
import com.gr6.SmartCart.module_v3.recommendation.ai.dto.AiTextQueryRequest;
import com.gr6.SmartCart.module_v3.recommendation.dto.RecommendedProductDTO;
import com.gr6.SmartCart.module_v3.recommendation.event.UserProductEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiRecommendationService {

    private final AiProductRepository productRepository;
    private final AiServerClient aiServerClient;
    private final UserProductEventRepository eventRepository;

    private volatile long lastAiIndexAt = 0L;
    private volatile boolean aiIndexing = false;

    private static final long AI_INDEX_TTL_MS = 10 * 60 * 1000L;
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static final List<OrderStatus> FINISHED_ORDER_STATUS =
            List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED);

    public Map<String, Object> getTrending(int page, int size) {
        return recommendByText(
                "sản phẩm phổ biến bán chạy chất lượng tốt giá hợp lý",
                Set.of(),
                Set.of(),
                null,
                page,
                size,
                "ai-trending",
                true
        );
    }

    public Map<String, Object> searchByKeyword(
            String keyword,
            int page,
            int size
    ) {
        String seedText = keyword == null ? "" : keyword.trim();

        if (seedText.isBlank()) {
            return getTrending(page, size);
        }

        return recommendByText(
                expandKeyword(seedText),
                Set.of(),
                Set.of(),
                null,
                page,
                size,
                "ai-keyword-search",
                true
        );
    }

    public Map<String, Object> getSimilarByProduct(
            Long productId,
            int page,
            int size
    ) {
        if (productId == null || productId <= 0) {
            return getTrending(page, size);
        }

        Optional<Product> optionalProduct = productRepository.findById(productId);

        if (optionalProduct.isEmpty()) {
            return getTrending(page, size);
        }

        Product product = optionalProduct.get();

        if (product.getCategory() == null || product.getCategory().getCategoryId() == null) {
            return getTrending(page, size);
        }

        List<Product> sameCategoryProducts = productRepository.findSameCategoryCandidates(
                product.getProductId(),
                product.getCategory().getCategoryId(),
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                CategoryStatus.ACTIVE,
                VariantStatus.ACTIVE
        );

        Set<Long> allowedIds = sameCategoryProducts.stream()
                .map(Product::getProductId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        String seedText = buildSimilarSeedText(product);

        return recommendByText(
                seedText,
                Set.of(product.getProductId()),
                allowedIds,
                sameCategoryProducts,
                page,
                Math.min(normalizeSize(size), 8),
                "ai-product-similar-same-category",
                true
        );
    }

    public Map<String, Object> getPersonal(
            String email,
            int page,
            int size
    ) {
        if (email == null || email.isBlank()) {
            return getTrending(page, size);
        }

        List<String> seedParts = new ArrayList<>();

        List<UserProductEvent> searchEvents = eventRepository.findRecentSearchEvents(
                email,
                RecommendationEventType.SEARCH,
                org.springframework.data.domain.PageRequest.of(0, 20)
        );

        int searchRank = 0;
        for (UserProductEvent event : safeList(searchEvents)) {
            if (event.getKeyword() == null || event.getKeyword().trim().isEmpty()) continue;

            int weight = Math.max(5, 18 - searchRank);
            addWeighted(seedParts, expandKeyword(event.getKeyword()), weight);
            searchRank++;
        }

        List<UserProductEvent> behaviorEvents = eventRepository.findRecentEvents(
                email,
                List.of(
                        RecommendationEventType.VIEW_PRODUCT,
                        RecommendationEventType.ADD_TO_CART,
                        RecommendationEventType.PURCHASE
                ),
                ProductStatus.ACTIVE,
                org.springframework.data.domain.PageRequest.of(0, 80)
        );

        int rank = 0;
        for (UserProductEvent event : safeList(behaviorEvents)) {
            if (event.getProduct() == null) continue;

            int baseWeight;

            if (event.getEventType() == RecommendationEventType.VIEW_PRODUCT) {
                baseWeight = 14;
            } else if (event.getEventType() == RecommendationEventType.ADD_TO_CART) {
                baseWeight = 9;
            } else if (event.getEventType() == RecommendationEventType.PURCHASE) {
                baseWeight = 8;
            } else {
                baseWeight = 3;
            }

            int recencyBonus = Math.max(0, 12 - rank / 4);
            int quantityBonus = event.getQuantity() == null ? 0 : Math.min(event.getQuantity(), 5);

            int finalWeight = baseWeight + recencyBonus + quantityBonus;

            addWeighted(seedParts, buildProductText(event.getProduct()), finalWeight);

            if (event.getProduct().getCategory() != null) {
                addWeighted(seedParts, event.getProduct().getCategory().getCategoryName(), Math.max(3, finalWeight / 3));
            }

            if (event.getProduct().getBrand() != null && !event.getProduct().getBrand().isBlank()) {
                addWeighted(seedParts, event.getProduct().getBrand(), Math.max(2, finalWeight / 4));
            }

            rank++;
        }

        if (seedParts.isEmpty()) {
            return getTrending(page, size);
        }

        String seedText = String.join(" | ", seedParts);

        return recommendByText(
                seedText,
                Set.of(),
                Set.of(),
                null,
                page,
                size,
                "ai-personal-shopee-behavior-search-view",
                true
        );
    }

    private void addWeighted(List<String> seedParts, String text, int weight) {
        if (text == null || text.trim().isEmpty()) return;

        int safeWeight = Math.max(1, Math.min(weight, 25));

        for (int i = 0; i < safeWeight; i++) {
            seedParts.add(text.trim());
        }
    }

    public Map<String, Object> searchByImage(
            MultipartFile file,
            int page,
            int size
    ) {
        List<Product> activeProducts = findActiveProducts();
        Map<Long, Product> productMap = toProductMap(activeProducts);

        Map<Long, double[]> ratingMap = getRatingMap();
        Map<Long, BigDecimal[]> priceMap = getPriceMap();
        Map<Long, Integer> soldMap = getSoldMap();

        ensureAiIndexAsync(activeProducts, ratingMap, soldMap);

        AiRecommendResponse aiResponse = aiServerClient.searchByImage(
                file,
                normalizePage(page),
                normalizeSize(size)
        );

        return buildResponseFromAi(
                aiResponse,
                productMap,
                ratingMap,
                priceMap,
                soldMap,
                "ai-image-search",
                false
        );
    }

    public Map<String, Object> forceRebuildIndex() {
        List<Product> activeProducts = findActiveProducts();
        Map<Long, double[]> ratingMap = getRatingMap();
        Map<Long, Integer> soldMap = getSoldMap();

        List<AiProductCandidate> candidates = activeProducts.stream()
                .map(product -> toCandidate(product, ratingMap, soldMap))
                .filter(candidate -> candidate.getProductId() != null && candidate.getProductId() > 0)
                .toList();

        boolean success = aiServerClient.indexProducts(candidates);

        if (success) {
            lastAiIndexAt = System.currentTimeMillis();
        } else {
            lastAiIndexAt = 0L;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("activeProducts", activeProducts.size());
        result.put("candidates", candidates.size());
        result.put("message", success ? "AI index rebuilt" : "AI index rebuild failed");

        return result;
    }

    private Map<String, Object> recommendByText(
            String seedText,
            Set<Long> excludeProductIds,
            Set<Long> allowedProductIds,
            List<Product> responseProductScope,
            int page,
            int size,
            String strategy,
            boolean allowFallback
    ) {
        List<Product> activeProducts = findActiveProducts();

        List<Product> responseProducts = responseProductScope == null
                ? activeProducts
                : responseProductScope;

        Map<Long, Product> productMap = toProductMap(responseProducts);

        Map<Long, double[]> ratingMap = getRatingMap();
        Map<Long, BigDecimal[]> priceMap = getPriceMap();
        Map<Long, Integer> soldMap = getSoldMap();

        ensureAiIndexAsync(activeProducts, ratingMap, soldMap);

        AiTextQueryRequest request = new AiTextQueryRequest(
                seedText,
                excludeProductIds == null ? List.of() : new ArrayList<>(excludeProductIds),
                allowedProductIds == null ? List.of() : new ArrayList<>(allowedProductIds),
                normalizePage(page),
                normalizeSize(size)
        );

        AiRecommendResponse aiResponse = aiServerClient.recommendByText(request);

        return buildResponseFromAi(
                aiResponse,
                productMap,
                ratingMap,
                priceMap,
                soldMap,
                strategy,
                allowFallback
        );
    }

    private void ensureAiIndexAsync(
            List<Product> activeProducts,
            Map<Long, double[]> ratingMap,
            Map<Long, Integer> soldMap
    ) {
        long now = System.currentTimeMillis();

        if (now - lastAiIndexAt < AI_INDEX_TTL_MS) {
            return;
        }

        if (aiIndexing) {
            return;
        }

        aiIndexing = true;

        CompletableFuture.runAsync(() -> {
            try {
                List<AiProductCandidate> candidates = activeProducts.stream()
                        .map(product -> toCandidate(product, ratingMap, soldMap))
                        .filter(candidate -> candidate.getProductId() != null && candidate.getProductId() > 0)
                        .toList();

                System.out.println("AI ASYNC INDEX candidates size = " + candidates.size());

                if (candidates.isEmpty()) {
                    lastAiIndexAt = 0L;
                    return;
                }

                boolean success = aiServerClient.indexProducts(candidates);

                if (success) {
                    lastAiIndexAt = System.currentTimeMillis();
                } else {
                    lastAiIndexAt = 0L;
                }
            } finally {
                aiIndexing = false;
            }
        });
    }

    private Map<String, Object> buildResponseFromAi(
            AiRecommendResponse aiResponse,
            Map<Long, Product> productMap,
            Map<Long, double[]> ratingMap,
            Map<Long, BigDecimal[]> priceMap,
            Map<Long, Integer> soldMap,
            String strategy,
            boolean allowFallback
    ) {
        if (aiResponse == null || aiResponse.getItems() == null || aiResponse.getItems().isEmpty()) {
            if (!allowFallback) {
                return buildPage(List.of(), 0, 10, 0, 0, false, strategy);
            }

            List<RecommendedProductDTO> fallbackProducts = buildFallbackProducts(
                    productMap,
                    ratingMap,
                    priceMap,
                    soldMap,
                    "Sản phẩm phù hợp trên SmartCart"
            );

            return buildPage(
                    fallbackProducts,
                    0,
                    10,
                    fallbackProducts.size(),
                    fallbackProducts.isEmpty() ? 0 : 1,
                    false,
                    strategy + "-fallback"
            );
        }

        List<RecommendedProductDTO> products = new ArrayList<>();

        for (AiRecommendItem item : aiResponse.getItems()) {
            if (item == null || item.getProductId() == null) continue;

            Product product = productMap.get(item.getProductId());

            if (product == null) continue;

            products.add(toRecommendedDTO(
                    product,
                    item.getScore(),
                    item.getReason(),
                    ratingMap,
                    priceMap,
                    soldMap
            ));
        }

        if (products.isEmpty()) {
            if (!allowFallback) {
                return buildPage(
                        List.of(),
                        aiResponse.getPage(),
                        aiResponse.getSize(),
                        0,
                        0,
                        false,
                        strategy
                );
            }

            List<RecommendedProductDTO> fallbackProducts = buildFallbackProducts(
                    productMap,
                    ratingMap,
                    priceMap,
                    soldMap,
                    "Sản phẩm phù hợp trên SmartCart"
            );

            return buildPage(
                    fallbackProducts,
                    0,
                    10,
                    fallbackProducts.size(),
                    fallbackProducts.isEmpty() ? 0 : 1,
                    false,
                    strategy + "-fallback"
            );
        }

        return buildPage(
                products,
                aiResponse.getPage(),
                aiResponse.getSize(),
                aiResponse.getTotalElements(),
                aiResponse.getTotalPages(),
                aiResponse.isHasMore(),
                strategy
        );
    }

    private List<RecommendedProductDTO> buildFallbackProducts(
            Map<Long, Product> productMap,
            Map<Long, double[]> ratingMap,
            Map<Long, BigDecimal[]> priceMap,
            Map<Long, Integer> soldMap,
            String reason
    ) {
        return productMap.values()
                .stream()
                .sorted(Comparator
                        .comparing((Product product) -> soldMap.getOrDefault(product.getProductId(), 0))
                        .reversed()
                        .thenComparing(Product::getProductId, Comparator.reverseOrder())
                )
                .limit(10)
                .map(product -> toRecommendedDTO(
                        product,
                        0.0,
                        reason,
                        ratingMap,
                        priceMap,
                        soldMap
                ))
                .toList();
    }

    private Map<String, Object> buildPage(
            List<RecommendedProductDTO> products,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasMore,
            String strategy
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("strategy", strategy);
        response.put("model", "MiniLM + OWL-ViT + DINOv2 visual index");
        response.put("products", products);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("hasMore", hasMore);
        return response;
    }

    private AiProductCandidate toCandidate(
            Product product,
            Map<Long, double[]> ratingMap,
            Map<Long, Integer> soldMap
    ) {
        double rating = 0.0;
        long reviewCount = 0L;

        double[] ratingData = ratingMap.get(product.getProductId());

        if (ratingData != null) {
            rating = ratingData[0];
            reviewCount = (long) ratingData[1];
        }

        List<String> images = resolveProductImages(product);
        String mainImage = images.isEmpty() ? null : images.get(0);

        Long categoryId = null;
        String categoryName = "";

        if (product.getCategory() != null) {
            categoryId = product.getCategory().getCategoryId();
            categoryName = product.getCategory().getCategoryName();
        }

        return new AiProductCandidate(
                product.getProductId(),
                buildProductText(product),
                mainImage,
                images,
                categoryId,
                categoryName,
                product.getBrand(),
                product.getName(),
                soldMap.getOrDefault(product.getProductId(), 0),
                rating,
                reviewCount
        );
    }


    private RecommendedProductDTO toRecommendedDTO(
            Product product,
            Double score,
            String reason,
            Map<Long, double[]> ratingMap,
            Map<Long, BigDecimal[]> priceMap,
            Map<Long, Integer> soldMap
    ) {
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        BigDecimal[] priceRange = priceMap.get(product.getProductId());

        if (priceRange != null) {
            minPrice = priceRange[0];
            maxPrice = priceRange[1];
        }

        Double averageRating = 0.0;
        Long reviewCount = 0L;

        double[] ratingData = ratingMap.get(product.getProductId());

        if (ratingData != null) {
            averageRating = Math.round(ratingData[0] * 10.0) / 10.0;
            reviewCount = (long) ratingData[1];
        }

        return RecommendedProductDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .imageUrl(resolveProductImage(product))
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .shopName(product.getShop() == null ? "SmartCart Shop" : product.getShop().getShopName())
                .shopId(product.getShop() == null ? null : product.getShop().getShopId())
                .categoryName(product.getCategory() == null ? "Danh mục" : product.getCategory().getCategoryName())
                .soldCount(soldMap.getOrDefault(product.getProductId(), 0))
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .score(score == null ? 0.0 : score)
                .reason(reason == null || reason.isBlank() ? "AI gợi ý sản phẩm phù hợp" : reason)
                .build();
    }

    private List<Product> findActiveProducts() {
        return productRepository.findActiveProducts(
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                CategoryStatus.ACTIVE,
                VariantStatus.ACTIVE
        );
    }

    private Map<Long, Product> toProductMap(List<Product> products) {
        return safeList(products).stream()
                .filter(product -> product != null && product.getProductId() != null)
                .collect(Collectors.toMap(
                        Product::getProductId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private String buildSimilarSeedText(Product product) {
        String name = safe(product.getName());
        String brand = safe(product.getBrand());
        String category = product.getCategory() == null ? "" : safe(product.getCategory().getCategoryName());
        String description = safe(product.getDescription());

        String normalized = removeAccent((name + " " + description).toLowerCase());

        List<String> importantWords = new ArrayList<>();

        if (normalized.contains("ao thun") || normalized.contains("t-shirt") || normalized.contains("t shirt")) {
            importantWords.add("áo thun");
            importantWords.add("áo phông");
            importantWords.add("cotton");
            importantWords.add("form rộng");
            importantWords.add("thời trang");
        }

        if (normalized.contains("nike")) {
            importantWords.add("Nike");
            importantWords.add("thể thao");
        }

        if (normalized.contains("quan")) {
            importantWords.add("quần");
            importantWords.add("thời trang");
        }

        if (normalized.contains("giay")) {
            importantWords.add("giày");
            importantWords.add("sneaker");
            importantWords.add("thể thao");
        }

        return String.join(
                " ",
                name,
                brand,
                category,
                description,
                String.join(" ", importantWords)
        ).trim();
    }

    private String buildProductText(Product product) {
        if (product == null) return "";

        String name = safe(product.getName());
        String description = safe(product.getDescription());
        String brand = safe(product.getBrand());

        String category = product.getCategory() == null
                ? ""
                : safe(product.getCategory().getCategoryName());

        String shop = product.getShop() == null
                ? ""
                : safe(product.getShop().getShopName());

        return String.join(
                " ",
                name,
                brand,
                category,
                description,
                shop,
                expandKeyword(name),
                expandKeyword(category)
        ).trim();
    }

    private String expandKeyword(String keyword) {
        if (keyword == null) return "";

        String raw = keyword.trim();
        String normalized = removeAccent(raw).toLowerCase();

        List<String> parts = new ArrayList<>();
        parts.add(raw);

        if (normalized.contains("quan ao") || normalized.contains("quanao")) {
            parts.add("quần áo");
            parts.add("thời trang");
            parts.add("áo thun");
            parts.add("quần jean");
            parts.add("áo khoác");
        }

        if (normalized.contains("ao thun") || normalized.contains("aothun")) {
            parts.add("áo thun");
            parts.add("áo cotton");
            parts.add("áo phông");
            parts.add("t-shirt");
        }

        if (normalized.contains("dien thoai") || normalized.contains("dienthoai")) {
            parts.add("điện thoại");
            parts.add("smartphone");
            parts.add("phụ kiện điện thoại");
        }

        if (normalized.contains("giay")) {
            parts.add("giày");
            parts.add("sneaker");
            parts.add("giày thể thao");
        }

        return String.join(" ", parts);
    }

    private Map<Long, double[]> getRatingMap() {
        Map<Long, double[]> map = new HashMap<>();

        List<Object[]> rows = productRepository.findRatingStats();

        if (rows == null) return map;

        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            double rating = ((Number) row[1]).doubleValue();
            double reviewCount = ((Number) row[2]).doubleValue();

            map.put(productId, new double[]{rating, reviewCount});
        }

        return map;
    }

    private Map<Long, Integer> getSoldMap() {
        Map<Long, Integer> map = new HashMap<>();

        List<Object[]> rows = productRepository.findSoldStats(FINISHED_ORDER_STATUS);

        if (rows == null) return map;

        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            Integer sold = ((Number) row[1]).intValue();

            map.put(productId, sold);
        }

        return map;
    }

    private Map<Long, BigDecimal[]> getPriceMap() {
        Map<Long, BigDecimal[]> map = new HashMap<>();

        List<Object[]> rows = productRepository.findPriceRanges(VariantStatus.ACTIVE);

        if (rows == null) return map;

        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            BigDecimal minPrice = (BigDecimal) row[1];
            BigDecimal maxPrice = (BigDecimal) row[2];

            map.put(productId, new BigDecimal[]{minPrice, maxPrice});
        }

        return map;
    }

    private List<String> resolveProductImages(Product product) {
        List<String> result = new ArrayList<>();

        if (product == null) {
            return result;
        }

        // 1. Lấy ảnh chính từ products.image_urls
        if (product.getImageUrls() != null && !product.getImageUrls().isBlank()) {
            String raw = product.getImageUrls()
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .replace("\\", "");

            String[] parts = raw.split(",");

            for (String part : parts) {
                if (part == null) continue;

                String url = part.trim();

                if (url.startsWith("http://") || url.startsWith("https://")) {
                    result.add(url);
                }
            }
        }

        // 2. Lấy thêm ảnh từ biến thể sản phẩm
        if (product.getVariants() != null) {
            product.getVariants()
                    .stream()
                    .filter(variant -> variant != null)
                    .filter(variant -> variant.getImageUrl() != null)
                    .map(variant -> variant.getImageUrl().trim())
                    .filter(url -> !url.isBlank())
                    .filter(url -> url.startsWith("http://") || url.startsWith("https://"))
                    .forEach(result::add);
        }

        // 3. Xóa ảnh trùng nhau
        List<String> unique = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String url : result) {
            if (url == null || url.isBlank()) continue;

            String cleanUrl = url.trim();

            if (seen.contains(cleanUrl)) continue;

            seen.add(cleanUrl);
            unique.add(cleanUrl);

            // Không gửi quá nhiều ảnh cho 1 sản phẩm
            if (unique.size() >= 8) {
                break;
            }
        }

        return unique;
    }

    private String resolveProductImage(Product product) {
        if (product == null) return null;

        String imageUrl = extractFirstImage(product.getImageUrls());

        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl;
        }

        if (product.getVariants() != null) {
            return product.getVariants()
                    .stream()
                    .filter(v -> v != null && v.getImageUrl() != null && !v.getImageUrl().isBlank())
                    .sorted((a, b) -> {
                        boolean da = Boolean.TRUE.equals(a.getIsDefault());
                        boolean db = Boolean.TRUE.equals(b.getIsDefault());

                        if (da == db) return 0;

                        return da ? -1 : 1;
                    })
                    .map(ProductVariant::getImageUrl)
                    .map(String::trim)
                    .filter(url -> url.startsWith("http://") || url.startsWith("https://"))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    private String extractFirstImage(String imageUrls) {
        if (imageUrls == null || imageUrls.isBlank()) return null;

        String raw = imageUrls.trim();

        if (raw.startsWith("[") && raw.endsWith("]")) {
            raw = raw
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .replace("\\", "");
        } else {
            raw = raw
                    .replace("\"", "")
                    .replace("\\", "");
        }

        String[] parts = raw.split(",");

        for (String part : parts) {
            if (part == null) continue;

            String url = part.trim();

            if (!url.isBlank()
                    && (url.startsWith("http://") || url.startsWith("https://"))) {
                return url;
            }
        }

        return null;
    }

    private String removeAccent(String value) {
        if (value == null) return "";

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return DIACRITICS.matcher(normalized)
                .replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D");
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size <= 0) return 10;
        return Math.min(size, 50);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}