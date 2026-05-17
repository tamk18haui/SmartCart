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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DiscoveryService {

    @Autowired
    private StorefrontProductRepository productRepository;

    @Autowired
    private CatalogReviewRepository catalogReviewRepository;

    @Autowired
    private CatalogOrderItemRepository catalogOrderItemRepository;

    @Transactional (readOnly = true)
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
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional (readOnly = true)
    public Page<ProductResponseDTO> searchAndFilterProducts(
            SearchFilterRequest request,
            int page,
            int size
    ) {
        if (request == null) {
            request = new SearchFilterRequest();
        }

        String keyword = request.getKeyword() == null
                ? ""
                : request.getKeyword().trim();

        Long categoryId = request.getCategoryId();
        BigDecimal minPrice = request.getMinPrice();
        BigDecimal maxPrice = request.getMaxPrice();

        Pageable pageable = PageRequest.of(page, size);

        Page<Product> productPage = productRepository.searchActiveProducts(
                keyword,
                categoryId,
                minPrice,
                maxPrice,
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
            dto.setLocation(extractProvince(product.getShop().getPickupAddress()));
        }

        dto.setName(product.getName());

        dto.setPrice(minPrice);
        dto.setMinPrice(minPrice);
        dto.setMaxPrice(maxPrice);

        if (product.getBasePrice() != null && product.getBasePrice().compareTo(minPrice) > 0) {
            dto.setOriginalPrice(product.getBasePrice());
        } else {
            dto.setOriginalPrice(null);
        }

        dto.setImageUrl(getDisplayImage(product, activeVariants));

        Integer soldQuantity = catalogOrderItemRepository.getSoldQuantityByProductId(
                product.getProductId(),
                List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED)
        );

        Double averageRating = catalogReviewRepository.getAverageRatingByProductId(product.getProductId());
        Integer reviewCount = catalogReviewRepository.getReviewCountByProductId(product.getProductId());

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
                .filter(variant -> variant.getStatus() == VariantStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    private BigDecimal getMinVariantPrice(Product product, List<ProductVariant> activeVariants) {
        return activeVariants.stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(product.getBasePrice() == null ? BigDecimal.ZERO : product.getBasePrice());
    }

    private BigDecimal getMaxVariantPrice(Product product, List<ProductVariant> activeVariants) {
        return activeVariants.stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(product.getBasePrice() == null ? BigDecimal.ZERO : product.getBasePrice());
    }

    private String getDisplayImage(Product product, List<ProductVariant> activeVariants) {
        if (activeVariants != null) {
            for (ProductVariant variant : activeVariants) {
                if (Boolean.TRUE.equals(variant.getIsDefault())
                        && !isBlank(variant.getImageUrl())) {
                    return variant.getImageUrl().trim();
                }
            }

            for (ProductVariant variant : activeVariants) {
                if (!isBlank(variant.getImageUrl())) {
                    return variant.getImageUrl().trim();
                }
            }
        }

        return firstImage(product == null ? null : product.getImageUrls());
    }

    private String firstImage(String imageUrls) {
        if (isBlank(imageUrls)) {
            return null;
        }

        return Arrays.stream(imageUrls.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .findFirst()
                .orElse(null);
    }

    private String extractProvince(String pickupAddress) {
        if (isBlank(pickupAddress)) {
            return "Việt Nam";
        }

        String[] parts = pickupAddress.split(",");
        if (parts.length == 0) {
            return pickupAddress.trim();
        }

        return parts[parts.length - 1].trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}