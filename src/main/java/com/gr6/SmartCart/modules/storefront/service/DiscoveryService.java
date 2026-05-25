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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DiscoveryService {

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

        return productPage.map(this::mapToDTO);
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

            // Frontend đang dùng location ở vài card cũ,
            // nên set luôn location = tên shop để không hiện địa chỉ nữa.
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
}