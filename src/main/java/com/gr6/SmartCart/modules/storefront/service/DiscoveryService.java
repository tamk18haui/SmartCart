package com.gr6.SmartCart.modules.storefront.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
 
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.modules.storefront.dto.ProductResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.SearchFilterRequest;
import com.gr6.SmartCart.modules.storefront.repository.StorefrontProductRepository;

@Service
public class DiscoveryService {

    @Autowired
    private StorefrontProductRepository productRepository;

    public List<ProductResponseDTO> getHomeProducts() {
        List<Product> products = productRepository.findTop10ByOrderByProductIdDesc();
        return products.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Page<ProductResponseDTO> searchAndFilterProducts(SearchFilterRequest request, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Product> productPage;

    String keyword = request.getKeyword() != null ? request.getKeyword() : "";
    Long categoryId = request.getCategoryId();
    Double minPrice = request.getMinPrice();
    Double maxPrice = request.getMaxPrice();

    // Check null trước khi unbox để tránh warning/NPE
    boolean hasMinPrice = Objects.nonNull(minPrice) && minPrice > 0;
    boolean hasMaxPrice = Objects.nonNull(maxPrice) && maxPrice < Double.MAX_VALUE;

    if (categoryId != null && hasMinPrice && hasMaxPrice) {
        productPage = productRepository.findByNameContainingIgnoreCaseAndCategory_CategoryIdAndBasePriceBetween(
            keyword, categoryId, minPrice, maxPrice, pageable);
    } else if (categoryId != null) {
        productPage = productRepository.findByNameContainingIgnoreCaseAndCategory_CategoryId(
            keyword, categoryId, pageable);
    } else if (hasMinPrice && hasMaxPrice) {
        productPage = productRepository.findByNameContainingIgnoreCaseAndBasePriceBetween(
            keyword, minPrice, maxPrice, pageable);
    } else if (!keyword.isEmpty()) {
        productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    } else {
        // Fallback: tìm tất cả với pagination (thay vì findAll() không pagination)
        productPage = productRepository.findAll(pageable);
    }

    return productPage.map(this::mapToDTO);
}


    // Hàm convert thủ công từ Entity sang DTO 
    private ProductResponseDTO mapToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getBasePrice());
        // Xử lý imageUrls: split và lấy ảnh đầu tiên nếu có
        String imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String[] urls = imageUrls.split(",");
            dto.setImageUrl(urls[0].trim()); // Lấy ảnh đầu tiên
        } else {
            dto.setImageUrl(null);
        }
        return dto;
    }
}