package com.gr6.SmartCart.modules.fulfillment.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductOptionValue;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Review;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.fulfillment.dto.ProductDetailResponse;
import com.gr6.SmartCart.modules.fulfillment.repository.ReviewRepository;

import com.gr6.SmartCart.modules.fulfillment.service.ProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductDetailServiceImpl implements ProductDetailService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<ProductDetailResponse> getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (product.getStatus() == ProductStatus.HIDDEN || product.getStatus() == ProductStatus.DELETED) {
            return BaseResponse.error(404, "Sản phẩm hiện không khả dụng.");
        }

        // 1. Nhóm các Option (Màu sắc, Kích cỡ) để Frontend vẽ nút
        List<ProductDetailResponse.OptionGroupDTO> optionGroups = product.getOptions().stream()
                .map(opt -> ProductDetailResponse.OptionGroupDTO.builder()
                        .name(opt.getName())
                        .values(opt.getValues().stream().map(ProductOptionValue::getValue).toList())
                        .build())
                .toList();

        // 2. Map danh sách biến thể kèm thuộc tính
        List<ProductDetailResponse.VariantDTO> variantDTOs = product.getVariants().stream()
                .map(v -> {
                    Map<String, String> attrs = new HashMap<>();
                    v.getVariantOptionValues().forEach(vov ->
                            attrs.put(vov.getOptionValue().getProductOption().getName(), vov.getOptionValue().getValue()));

                    return ProductDetailResponse.VariantDTO.builder()
                            .variantId(v.getVariantId())
                            .sku(v.getSku())
                            .price(v.getPrice())
                            .stockQuantity(v.getStockQuantity())
                            .imageUrl(v.getImageUrl())
                            .attributes(attrs)
                            .build();
                }).toList();


        // ==========================================================
        // SÁNG SỬA VÀO ĐÂY: Xử lý cắt chuỗi ảnh để trả về dạng Mảng
        // ==========================================================
        List<String> listImages = new ArrayList<>();
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            // Lấy chuỗi từ CSDL (vd: "link1,link2,link3") cắt ra bằng dấu phẩy
            listImages = Arrays.asList(product.getImageUrls().split(","));
        }

        // 3. Xây dựng Response cuối cùng
        ProductDetailResponse detail = ProductDetailResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                // SÁNG SỬA VÀO ĐÂY: Truyền listImages vào DTO để Frontend hiển thị slider nhiều ảnh
                .imageUrls(listImages)
                .shopName(product.getShop().getShopName())
                .totalStock(variantDTOs.stream().mapToInt(v -> v.getStockQuantity()).sum())
                .status("ACTIVE")
                .optionGroups(optionGroups)
                .variants(variantDTOs)
                .build();

        return BaseResponse.success_data("Lấy chi tiết sản phẩm thành công", detail);
    }
}