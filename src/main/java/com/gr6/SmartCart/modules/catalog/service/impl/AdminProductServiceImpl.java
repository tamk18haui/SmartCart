package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.service.AdminProductService;
import com.gr6.SmartCart.modules.catalog.dto.ProductResponse;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<ProductResponse>> getProducts(
            String keyword,
            ProductStatus status,
            Long shopId,
            Long categoryId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<ProductResponse> result = productRepository
                .searchForAdmin(normalizeKeyword(keyword), status, shopId, categoryId, pageable)
                .map(ProductResponse::fromEntity);

        return BaseResponse.success_data("Thành công", PageResponse.of(result));
    }

    @Override
    @Transactional
    public BaseResponse<String> banProduct(Long productId, String reason) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return BaseResponse.error(404, "Không tìm thấy sản phẩm");
        }
        if (product.getStatus() == ProductStatus.DELETED) {
            return BaseResponse.error(400, "Sản phẩm đã bị xóa, không thể khóa");
        }
        if (product.getStatus() == ProductStatus.BANNED) {
            return BaseResponse.error(400, "Sản phẩm đã bị khóa trước đó");
        }

        product.setPreviousStatus(product.getStatus() == null ? ProductStatus.HIDDEN : product.getStatus());
        product.setStatus(ProductStatus.BANNED);
        product.setBanReason(isBlank(reason) ? "Vi phạm chính sách sản phẩm" : reason.trim());
        product.setBannedAt(LocalDateTime.now());
        product.setBannedBy(currentAdminEmail());
        productRepository.save(product);

        return BaseResponse.successMessage("Đã khóa sản phẩm thành công");
    }

    @Override
    @Transactional
    public BaseResponse<String> unbanProduct(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return BaseResponse.error(404, "Không tìm thấy sản phẩm");
        }
        if (product.getStatus() == ProductStatus.DELETED) {
            return BaseResponse.error(400, "Sản phẩm đã bị xóa, không thể mở khóa");
        }
        if (product.getStatus() != ProductStatus.BANNED) {
            return BaseResponse.error(400, "Sản phẩm chưa bị khóa");
        }

        ProductStatus targetStatus = safeRestoreStatus(product);
        product.setStatus(targetStatus);
        product.setPreviousStatus(null);
        product.setBanReason(null);
        product.setBannedAt(null);
        product.setBannedBy(null);
        productRepository.save(product);

        return BaseResponse.successMessage("Đã mở khóa sản phẩm thành công");
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return BaseResponse.error(404, "Không tìm thấy sản phẩm");
        }
        if (product.getStatus() == ProductStatus.DELETED) {
            return BaseResponse.error(400, "Sản phẩm đã bị xóa trước đó");
        }

        product.setStatus(ProductStatus.DELETED);
        product.setPreviousStatus(null);
        product.setBanReason(null);
        product.setBannedAt(null);
        product.setBannedBy(null);

        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                if (variant.getStatus() != VariantStatus.DELETED) {
                    variant.setStatus(VariantStatus.DELETED);
                }
            }
        }

        productRepository.save(product);
        return BaseResponse.successMessage("Đã xóa sản phẩm thành công");
    }

    private ProductStatus safeRestoreStatus(Product product) {
        ProductStatus previous = product.getPreviousStatus();
        if (previous == null || previous == ProductStatus.BANNED || previous == ProductStatus.DELETED) {
            previous = ProductStatus.ACTIVE;
        }

        boolean shopActive = product.getShop() != null && product.getShop().getStatus() == ShopStatus.ACTIVE;
        boolean categoryActive = product.getCategory() != null && product.getCategory().getCategoryStatus() == CategoryStatus.ACTIVE;

        if (!shopActive || !categoryActive) {
            return ProductStatus.HIDDEN;
        }
        return previous;
    }

    private int normalizePage(int page) {
        return Math.max(page - 1, 0);
    }

    private int normalizeSize(int size) {
        if (size <= 0) return 10;
        return Math.min(size, 100);
    }

    private String normalizeKeyword(String keyword) {
        return isBlank(keyword) ? null : keyword.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String currentAdminEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "SYSTEM_ADMIN";
        }
        return authentication.getName();
    }
}