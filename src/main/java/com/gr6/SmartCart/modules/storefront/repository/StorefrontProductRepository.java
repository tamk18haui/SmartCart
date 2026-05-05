package com.gr6.SmartCart.modules.storefront.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gr6.SmartCart.common.domain.Product;

@Repository 
public interface StorefrontProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findTop10ByOrderByProductIdDesc();

    List<Product> findByNameContainingIgnoreCase(String keyword);
}