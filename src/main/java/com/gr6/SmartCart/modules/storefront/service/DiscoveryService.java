package com.gr6.SmartCart.modules.storefront.service;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscoveryService {

    @Autowired
    private StorefrontProductRepository productRepository;

    @Autowired
    private CatalogReviewRepository catalogReviewRepository;

    @Autowired
    private CatalogOrderItemRepository catalogOrderItemRepository;

    public List<ProductResponseDTO> getHomeProducts() {
        Pageable limit10 = PageRequest.of(0, 10);

        List<Product> products = productRepository.findTop10SellableProducts(
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                CategoryStatus.ACTIVE,
                limit10
        );

        return products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductResponseDTO> searchAndFilterProducts(SearchFilterRequest request, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String keyword = request != null && request.getKeyword() != null
                ? request.getKeyword().trim()
                : "";

        Long categoryId = request != null ? request.getCategoryId() : null;

        BigDecimal minPrice = request != null ? request.getMinPrice() : null;
        BigDecimal maxPrice = request != null ? request.getMaxPrice() : null;

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            minPrice = BigDecimal.ZERO;
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            maxPrice = BigDecimal.ZERO;
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new RuntimeException("Giá tối thiểu không được lớn hơn giá tối đa");
        }

        Page<Product> productPage = productRepository.searchActiveProducts(
                keyword,
                categoryId,
                minPrice,
                maxPrice,
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                CategoryStatus.ACTIVE,
                pageable
        );

        return productPage.map(this::mapToDTO);
    }

    private ProductResponseDTO mapToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getBasePrice());

        String imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isBlank()) {
            String[] urls = imageUrls.split(",");
            dto.setImageUrl(urls[0].trim());
        } else {
            dto.setImageUrl(null);
        }

        if (product.getShop() != null) {
            dto.setLocation(product.getShop().getPickupAddress());
        }

        Double averageRating = catalogReviewRepository.getAverageRatingByProductId(product.getProductId());
        dto.setAverageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);

        Integer soldQuantity = catalogOrderItemRepository.getSoldQuantityByProductId(
                product.getProductId(),
                OrderStatus.DELIVERED
        );
        dto.setSoldQuantity(soldQuantity != null ? soldQuantity : 0);

        return dto;
    }
}