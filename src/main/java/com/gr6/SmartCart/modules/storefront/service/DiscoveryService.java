package com.gr6.SmartCart.modules.storefront.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.modules.storefront.dto.SearchFilterRequest;
import com.gr6.SmartCart.modules.storefront.repository.StorefrontProductRepository;

@Service
public class DiscoveryService {

    @Autowired
    private StorefrontProductRepository productRepository;

    public List<Product> getHomeProducts() {
        // Lấy dữ liệu thật từ database
        return productRepository.findTop10ByOrderByProductIdDesc(); 
    }

    public List<Product> searchAndFilterProducts(SearchFilterRequest request) {
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(request.getKeyword());
        }
        return productRepository.findAll();
    }
}