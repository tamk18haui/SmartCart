package com.gr6.SmartCart.modules.catalog.dto;

import com.gr6.SmartCart.common.domain.Product;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProductResponse {
    private Long productId;
    private String name;
    private String description;
    private String brand;
    private BigDecimal basePrice;
    private Long categoryId;
    private Long shopId;
    private String status;
    private List<VariantResponse> variants;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setBasePrice(product.getBasePrice());
        response.setCategoryId(product.getCategory().getCategoryId());
        response.setShopId(product.getShop().getShopId());
        if(product.getStatus() != null) {
            response.setStatus(product.getStatus().name());
        }

        if (product.getVariants() != null) {
            response.setVariants(product.getVariants().stream()
                    .map(VariantResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        return response;
    }
}