package com.gr6.SmartCart.modules.storefront.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gr6.SmartCart.common.domain.Product;

@Repository 
public interface StorefrontProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findTop10ByOrderByProductIdDesc();

    List<Product> findByNameContainingIgnoreCase(String keyword);

    // Thêm methods cho filter
    Page<Product> findByNameContainingIgnoreCaseAndCategory_CategoryIdAndBasePriceBetween(
        String keyword, Long categoryId, Double minPrice, Double maxPrice, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndCategory_CategoryId(
        String keyword, Long categoryId, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndBasePriceBetween(
        String keyword, Double minPrice, Double maxPrice, Pageable pageable);

    Page<Product> findByCategory_CategoryIdAndBasePriceBetween(
        Long categoryId, Double minPrice, Double maxPrice, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Product> findByCategory_CategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByBasePriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // findAll(Pageable pageable) đã có sẵn từ JpaRepository, không cần declare lại
}