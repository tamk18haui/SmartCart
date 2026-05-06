package com.gr6.SmartCart.modules.fulfillment.service.impl;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Review;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.fulfillment.dto.ProductDetailResponse;
import com.gr6.SmartCart.modules.fulfillment.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductDetailServiceImpl {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long productId) {
        // 1. Lấy thông tin sản phẩm (Ném lỗi nếu không tìm thấy)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        // 2. Kiểm tra trạng thái rẽ nhánh (BANNED hoặc HIDDEN) [Yêu cầu UC 3.2.1]
        if ("BANNED".equals(product.getStatus().name()) || "HIDDEN".equals(product.getStatus().name())) {
            throw new RuntimeException("Sản phẩm hiện không khả dụng.");
        }

        // 3. Lấy danh sách đánh giá
        List<Review> reviews = reviewRepository.findByProductId(productId);

        // 4. Tính tổng tồn kho từ các biến thể [cite: 436]
        int totalStock = product.getVariants().stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();

        // 5. Map dữ liệu sang DTO [cite: 384]
        return ProductDetailResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .basePrice(product.getBasePrice())
                .shopName(product.getShop().getShopName())
                .totalStock(totalStock)
                .status(totalStock > 0 ? "Còn hàng" : "Hết hàng") // [Yêu cầu UC 3.2.2]
                .variants(product.getVariants().stream().map(v ->
                        ProductDetailResponse.VariantDTO.builder()
                                .variantId(v.getVariantId())
                                .sku(v.getSku())
                                .price(v.getPrice())
                                .stockQuantity(v.getStockQuantity())
                                .imageUrl(v.getImageUrl())
                                .build()
                ).collect(Collectors.toList()))
                .reviews(reviews.stream().map(r ->
                        ProductDetailResponse.ReviewDTO.builder()
                                .rating(r.getRating())
                                .comment(r.getComment())
                                .userName(r.getUser().getFullName())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}