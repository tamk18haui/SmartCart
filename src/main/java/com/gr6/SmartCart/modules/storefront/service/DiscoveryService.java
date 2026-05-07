package com.gr6.SmartCart.modules.storefront.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.modules.catalog.repository.CatalogOrderItemRepository;
import com.gr6.SmartCart.modules.catalog.repository.CatalogReviewRepository;
import com.gr6.SmartCart.modules.storefront.dto.ProductResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.SearchFilterRequest;
import com.gr6.SmartCart.modules.storefront.repository.StorefrontProductRepository;

@Service
public class DiscoveryService {

    @Autowired
    private StorefrontProductRepository productRepository;

    @Autowired
    private CatalogReviewRepository catalogReviewRepository;

    @Autowired
    private CatalogOrderItemRepository catalogOrderItemRepository;

    public List<ProductResponseDTO> getHomeProducts() {
        List<Product> products = productRepository.findTop10ByOrderByProductIdDesc();
        return products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductResponseDTO> searchAndFilterProducts(
            SearchFilterRequest request,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        String keyword = request.getKeyword() != null
                ? request.getKeyword()
                : "";

        Long categoryId = request.getCategoryId();

        boolean hasPriceFilter =
                request.getMinPrice() != null
                || request.getMaxPrice() != null;

        Double minPrice =
                Optional.ofNullable(request.getMinPrice())
                        .orElse(0.0);

        Double maxPrice =
                Optional.ofNullable(request.getMaxPrice())
                        .orElse(Double.MAX_VALUE);

        if (categoryId != null && hasPriceFilter) {

            productPage =
                    productRepository
                            .findByNameContainingIgnoreCaseAndCategory_CategoryIdAndBasePriceBetween(
                                    keyword,
                                    categoryId,
                                    minPrice,
                                    maxPrice,
                                    pageable);

        } else if (categoryId != null) {

            productPage =
                    productRepository
                            .findByNameContainingIgnoreCaseAndCategory_CategoryId(
                                    keyword,
                                    categoryId,
                                    pageable);

        } else if (hasPriceFilter) {

            productPage =
                    productRepository
                            .findByNameContainingIgnoreCaseAndBasePriceBetween(
                                    keyword,
                                    minPrice,
                                    maxPrice,
                                    pageable);

        } else if (!keyword.isEmpty()) {

            productPage =
                    productRepository
                            .findByNameContainingIgnoreCase(
                                    keyword,
                                    pageable);

        } else {

            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(this::mapToDTO);
    }

    private ProductResponseDTO mapToDTO(Product product) {

        ProductResponseDTO dto = new ProductResponseDTO();

        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getBasePrice());

        // Image
        String imageUrls = product.getImageUrls();

        if (imageUrls != null && !imageUrls.isEmpty()) {

            String[] urls = imageUrls.split(",");
            dto.setImageUrl(urls[0].trim());

        } else {

            dto.setImageUrl(null);
        }

        // Shop location
        if (product.getShop() != null) {
            dto.setLocation(product.getShop().getPickupAddress());
        }

        // Sold quantity
        Integer sold =
                catalogOrderItemRepository
                        .getSoldQuantityByProductId(
                                product.getProductId(),
                                OrderStatus.DELIVERED);

        dto.setSoldQuantity(sold != null ? sold : 0);

        // Average rating
        Double avgRating =
                catalogReviewRepository
                        .getAverageRatingByProductId(
                                product.getProductId());

        if (avgRating != null) {
            dto.setAverageRating(
                    Math.round(avgRating * 10.0) / 10.0);
        } else {
            dto.setAverageRating(0.0);
        }

        return dto;
    }
}