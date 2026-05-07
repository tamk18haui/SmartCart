package com.gr6.SmartCart.modules.storefront.service;

import java.util.List;
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

        // Khắc phục: Gán giá trị mặc định an toàn nếu người dùng chỉ truyền 1 mốc giá, và cho phép giá 0đ
        // Sửa lại đoạn khởi tạo minPrice và maxPrice
boolean hasPriceFilter = request.getMinPrice() != null || request.getMaxPrice() != null;

// Sử dụng Optional để xử lý null an toàn và chuẩn Java
Double minPrice = java.util.Optional.ofNullable(request.getMinPrice()).orElse(0.0);
Double maxPrice = java.util.Optional.ofNullable(request.getMaxPrice()).orElse(Double.MAX_VALUE);

        if (categoryId != null && hasPriceFilter) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndCategory_CategoryIdAndBasePriceBetween(
                keyword, categoryId, minPrice, maxPrice, pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndCategory_CategoryId(
                keyword, categoryId, pageable);
        } else if (hasPriceFilter) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndBasePriceBetween(
                keyword, minPrice, maxPrice, pageable);
        } else if (!keyword.isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
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
        
        String imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String[] urls = imageUrls.split(",");
            dto.setImageUrl(urls[0].trim());
        } else {
            dto.setImageUrl(null);
        }
        return dto;
    }
}