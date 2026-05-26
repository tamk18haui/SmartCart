package com.gr6.SmartCart.modules.storefront.service;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.repository.CatalogOrderItemRepository;
import com.gr6.SmartCart.modules.catalog.repository.CatalogReviewRepository;
import com.gr6.SmartCart.modules.storefront.dto.ProductResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.SearchFilterRequest;
import com.gr6.SmartCart.modules.storefront.repository.StorefrontProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DiscoveryService {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    @Autowired
    private StorefrontProductRepository productRepository;

    @Autowired
    private CatalogReviewRepository catalogReviewRepository;

    @Autowired
    private CatalogOrderItemRepository catalogOrderItemRepository;

    public List<ProductResponseDTO> getHomeProducts() {
        Pageable pageable = PageRequest.of(0, 10);

        List<Product> products = productRepository.findTop10SellableProducts(
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                CategoryStatus.ACTIVE,
                VariantStatus.ACTIVE,
                pageable
        );

        return products.stream()
                .filter(Objects::nonNull)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductResponseDTO> searchAndFilterProducts(
            SearchFilterRequest request,
            int page,
            int size
    ) {
        if (request == null) {
            request = new SearchFilterRequest();
        }

        String keyword = normalizeKeyword(request.getKeyword());
        Long categoryId = request.getCategoryId();
        BigDecimal minPrice = request.getMinPrice();
        BigDecimal maxPrice = request.getMaxPrice();
        String sortBy = normalizeSortBy(request.getSortBy());

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            BigDecimal temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp;
        }

        Pageable pageable = PageRequest.of(
                normalizePage(page),
                normalizeSize(size)
        );

        Page<Product> productPage = productRepository.searchActiveProducts(
                keyword,
                categoryId,
                minPrice,
                maxPrice,
                sortBy,
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                CategoryStatus.ACTIVE,
                VariantStatus.ACTIVE,
                pageable
        );

        if (productPage.hasContent() || keyword.isBlank()) {
            return productPage.map(this::mapToDTO);
        }

        return fuzzySearch(keyword, categoryId, minPrice, maxPrice, sortBy, pageable);
    }

    private Page<ProductResponseDTO> fuzzySearch(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            Pageable pageable
    ) {
        List<Product> candidates = productRepository.findSellableProductsForFuzzy(
                categoryId,
                minPrice,
                maxPrice,
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                CategoryStatus.ACTIVE,
                VariantStatus.ACTIVE
        );

        String normalizedKeyword = normalizeText(keyword);

        List<Product> matched = candidates.stream()
                .filter(product -> fuzzyScore(product, normalizedKeyword) >= 45)
                .sorted(buildFuzzyComparator(normalizedKeyword, sortBy))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), matched.size());

        List<ProductResponseDTO> content = new ArrayList<>();

        if (start < matched.size()) {
            content = matched.subList(start, end)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(content, pageable, matched.size());
    }

    private Comparator<Product> buildFuzzyComparator(String normalizedKeyword, String sortBy) {
        if ("newest".equals(sortBy)) {
            return Comparator.comparing(Product::getProductId, Comparator.nullsLast(Comparator.reverseOrder()));
        }

        if ("sold_desc".equals(sortBy)) {
            return Comparator
                    .comparing((Product p) -> safeInt(p.getSoldCount()))
                    .reversed()
                    .thenComparing(Product::getProductId, Comparator.nullsLast(Comparator.reverseOrder()));
        }

        return Comparator
                .comparing((Product p) -> fuzzyScore(p, normalizedKeyword))
                .reversed()
                .thenComparing(Product::getProductId, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private int fuzzyScore(Product product, String normalizedKeyword) {
        if (product == null || normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return 0;
        }

        String name = normalizeText(product.getName());
        String brand = normalizeText(product.getBrand());
        String category = product.getCategory() == null ? "" : normalizeText(product.getCategory().getCategoryName());
        String description = normalizeText(product.getDescription());
        String shop = product.getShop() == null ? "" : normalizeText(product.getShop().getShopName());

        String haystack = String.join(" ", name, brand, category, description, shop).trim();

        if (haystack.contains(normalizedKeyword)) {
            return 100;
        }

        int best = 0;

        for (String token : normalizedKeyword.split("\\s+")) {
            if (token.isBlank()) continue;

            if (haystack.contains(token)) {
                best += 35;
            }
        }

        for (String word : haystack.split("\\s+")) {
            if (word.isBlank()) continue;

            int distance = levenshtein(normalizedKeyword, word);
            int maxLen = Math.max(normalizedKeyword.length(), word.length());

            if (maxLen == 0) continue;

            int score = (int) Math.round((1.0 - (double) distance / maxLen) * 100);

            if (score > best) {
                best = score;
            }
        }

        if (normalizedKeyword.contains("quan ao")) {
            if (category.contains("thoi trang") || name.contains("ao") || name.contains("quan")) {
                best = Math.max(best, 90);
            }
        }

        if (normalizedKeyword.contains("ao thun")) {
            if (name.contains("ao thun") || description.contains("cotton") || category.contains("thoi trang")) {
                best = Math.max(best, 90);
            }
        }

        return Math.min(best, 100);
    }

    private int levenshtein(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";

        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1
                        ),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }

    private ProductResponseDTO mapToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();

        List<ProductVariant> activeVariants = getActiveVariants(product);

        BigDecimal minPrice = getMinVariantPrice(product, activeVariants);
        BigDecimal maxPrice = getMaxVariantPrice(product, activeVariants);

        dto.setProductId(product.getProductId());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getCategoryId());
            dto.setCategoryName(product.getCategory().getCategoryName());
        }

        if (product.getShop() != null) {
            dto.setShopId(product.getShop().getShopId());
            dto.setShopName(product.getShop().getShopName());
            dto.setLocation(product.getShop().getShopName());
        }

        dto.setName(product.getName());
        dto.setPrice(minPrice);
        dto.setMinPrice(minPrice);
        dto.setMaxPrice(maxPrice);

        if (product.getBasePrice() != null
                && minPrice != null
                && product.getBasePrice().compareTo(minPrice) > 0) {
            dto.setOriginalPrice(product.getBasePrice());
        } else {
            dto.setOriginalPrice(null);
        }

        dto.setImageUrl(getDisplayImage(product, activeVariants));

        Integer soldQuantity = catalogOrderItemRepository.getSoldQuantityByProductId(
                product.getProductId(),
                List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED)
        );

        Double averageRating = catalogReviewRepository.getAverageRatingByProductId(
                product.getProductId()
        );

        Integer reviewCount = catalogReviewRepository.getReviewCountByProductId(
                product.getProductId()
        );

        dto.setSoldQuantity(soldQuantity == null ? 0 : soldQuantity);
        dto.setAverageRating(averageRating == null ? 0.0 : averageRating);
        dto.setReviewCount(reviewCount == null ? 0 : reviewCount);

        return dto;
    }

    private List<ProductVariant> getActiveVariants(Product product) {
        if (product == null || product.getVariants() == null) {
            return List.of();
        }

        return product.getVariants()
                .stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getStatus() == VariantStatus.ACTIVE)
                .filter(v -> v.getStockQuantity() != null && v.getStockQuantity() > 0)
                .collect(Collectors.toList());
    }

    private BigDecimal getMinVariantPrice(
            Product product,
            List<ProductVariant> activeVariants
    ) {
        if (activeVariants != null && !activeVariants.isEmpty()) {
            return activeVariants.stream()
                    .map(ProductVariant::getPrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(product.getBasePrice());
        }

        return product.getBasePrice();
    }

    private BigDecimal getMaxVariantPrice(
            Product product,
            List<ProductVariant> activeVariants
    ) {
        if (activeVariants != null && !activeVariants.isEmpty()) {
            return activeVariants.stream()
                    .map(ProductVariant::getPrice)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(product.getBasePrice());
        }

        return product.getBasePrice();
    }

    private String getDisplayImage(
            Product product,
            List<ProductVariant> activeVariants
    ) {
        if (activeVariants != null) {
            for (ProductVariant variant : activeVariants) {
                if (variant.getImageUrl() != null && !variant.getImageUrl().trim().isEmpty()) {
                    return variant.getImageUrl().trim();
                }
            }
        }

        if (product.getImageUrls() == null || product.getImageUrls().trim().isEmpty()) {
            return null;
        }

        return Arrays.stream(product.getImageUrls().split(","))
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .findFirst()
                .orElse(null);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return "";
        return keyword.trim();
    }

    private String normalizeText(String value) {
        if (value == null) return "";

        String normalized = Normalizer.normalize(value.trim().toLowerCase(), Normalizer.Form.NFD);

        return DIACRITICS.matcher(normalized)
                .replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size <= 0) return 20;
        return Math.min(size, 50);
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "relevance";
        }

        String value = sortBy.trim().toLowerCase();

        switch (value) {
            case "newest":
            case "sold_desc":
            case "price_asc":
            case "price_desc":
            case "relevance":
                return value;

            default:
                return "relevance";
        }
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}