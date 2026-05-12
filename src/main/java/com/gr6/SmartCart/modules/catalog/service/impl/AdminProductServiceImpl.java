package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.modules.catalog.dto.ProductResponse;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.catalog.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;

    @Override
    public BaseResponse<PageResponse<ProductResponse>> getProducts(int page, int size, ProductStatus status, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> products = productRepository.searchForAdmin(status, keyword, pageable);
        return BaseResponse.success_data("Thành công", PageResponse.of(products.map(ProductResponse::fromEntity)));
    }

    @Override
    @Transactional
    public BaseResponse<String> banProduct(Long productId, String reason) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        if (product.getStatus() == ProductStatus.DELETED) throw new RuntimeException("Không thể khóa sản phẩm đã xóa");

        if (product.getStatus() != ProductStatus.BANNED) {
            product.setPreviousStatus(product.getStatus());
        }

        product.setStatus(ProductStatus.BANNED);
        product.setBanReason(reason);
        product.setBannedAt(LocalDateTime.now());
        product.setBannedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        productRepository.save(product);
        return BaseResponse.successMessage("Đã khóa sản phẩm: " + product.getName());
    }

    @Override
    @Transactional
    public BaseResponse<String> unbanProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        if (product.getStatus() != ProductStatus.BANNED) throw new RuntimeException("Sản phẩm không ở trạng thái bị khóa");

        product.setStatus(ProductStatus.HIDDEN);
        product.setBanReason(null);
        product.setBannedAt(null);
        product.setBannedBy(null);

        productRepository.save(product);
        return BaseResponse.successMessage("Đã mở khóa sản phẩm: " + product.getName());
    }
}