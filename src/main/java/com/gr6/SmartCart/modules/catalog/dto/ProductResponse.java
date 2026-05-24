package com.gr6.SmartCart.modules.catalog.dto;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.VariantStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProductResponse {

    private Long productId;
    private String name;
    private String description;
    private String brand;
    private String condition;
    private BigDecimal basePrice;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Long categoryId;
    private Long shopId;
    private String status;
    private List<String> images;
    private List<VariantResponse> variants;
    private Double averageRating;
    private Integer reviewCount;
    private Integer soldQuantity;
    private Long totalRevenue;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();

        response.setProductId(product.getProductId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setBasePrice(product.getBasePrice());
        response.setWeight(product.getWeight());
        response.setLength(product.getLength());
        response.setWidth(product.getWidth());
        response.setHeight(product.getHeight());

        if (product.getCondition() != null) {
            response.setCondition(product.getCondition().name());
        }

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getCategoryId());
        }

        if (product.getShop() != null) {
            response.setShopId(product.getShop().getShopId());
        }

        if (product.getStatus() != null) {
            response.setStatus(product.getStatus().name());
        }

        if (product.getImageUrls() != null && !product.getImageUrls().isBlank()) {
            response.setImages(
                    Arrays.stream(product.getImageUrls().split(","))
                            .map(String::trim)
                            .filter(url -> !url.isBlank())
                            .collect(Collectors.toList())
            );
        }

        if (product.getVariants() != null) {
            response.setVariants(
                    product.getVariants()
                            .stream()
                            .filter(variant -> variant.getStatus() == null || variant.getStatus() != VariantStatus.DELETED)
                            .map(VariantResponse::fromEntity)
                            .collect(Collectors.toList())
            );
        }

        response.setAverageRating(0.0);
        response.setReviewCount(0);
        response.setSoldQuantity(0);
        response.setTotalRevenue(0L);

        return response;
    }
}


