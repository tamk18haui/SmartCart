package com.gr6.SmartCart.modules.fulfillment.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.modules.fulfillment.dto.ProductDetailResponse;
import com.gr6.SmartCart.modules.fulfillment.service.ProductDetailService;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository; // Gọi từ đảo Sáng
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductDetailServiceImpl implements ProductDetailService {

    private final ProductRepository productRepository;

    @Override
    public BaseResponse<ProductDetailResponse> getProductDetail(Long id) {
        // 1. Tìm sản phẩm theo ID
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        // 2. Tính tổng số lượng tồn kho từ danh sách biến thể (ProductVariants)
        // Giả sử trong Entity Product của bạn có: List<ProductVariant> variants
        Integer totalStock = 0;
        if (product.getVariants() != null) {
            totalStock = product.getVariants().stream()
                    .mapToInt(variant -> variant.getStockQuantity()) // Đây chính là trường trong file docx của bạn
                    .sum();
        }

        // 3. Đổ dữ liệu vào DTO
        ProductDetailResponse response = ProductDetailResponse.builder()
                .productId(product.getProductId())
                .productName(product.getName())
                .description(product.getDescription())
                .price(product.getBasePrice())
                .stockQuantity(totalStock) // Gán tổng số kho vừa tính được vào đây
                .shopName(product.getShop() != null ? product.getShop().getShopName() : "N/A")
                .status(product.getStatus() != null ? product.getStatus().toString() : "ACTIVE")
                .build();

        return BaseResponse.success_data("Lấy chi tiết sản phẩm thành công!", response);
    }
}