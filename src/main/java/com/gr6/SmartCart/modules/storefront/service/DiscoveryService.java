package com.gr6.SmartCart.modules.storefront.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

    public List<ProductResponseDTO> searchAndFilterProducts(SearchFilterRequest request) {
        List<Product> products;
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCase(request.getKeyword());
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Hàm convert thủ công từ Entity sang DTO (Thực tế đi làm hay dùng thư viện MapStruct hoặc ModelMapper)
    private ProductResponseDTO mapToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getBasePrice()); 
        dto.setImageUrl(product.getImageUrls()); // Tạm lấy text chứa url, thực tế có thể cần split lấy ảnh đầu tiên
        return dto;
    }
}