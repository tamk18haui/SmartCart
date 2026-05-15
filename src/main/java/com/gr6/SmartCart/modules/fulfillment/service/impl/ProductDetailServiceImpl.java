package com.gr6.SmartCart.modules.fulfillment.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductOption;
import com.gr6.SmartCart.common.domain.ProductOptionValue;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Review;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.fulfillment.dto.ProductDetailResponse;
import com.gr6.SmartCart.modules.fulfillment.repository.ReviewRepository;
import com.gr6.SmartCart.modules.fulfillment.service.ProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        if (!isSellableProduct(product)) {
            return BaseResponse.error(404, "Sản phẩm hiện không khả dụng.");
        }

        List<ProductVariant> activeVariants = safeList(product.getVariants()).stream()
                .filter(v -> v.getStatus() == VariantStatus.ACTIVE)
                .toList();

        if (activeVariants.isEmpty()) {
            return BaseResponse.error(404, "Sản phẩm hiện không còn phân loại khả dụng.");
        }

        List<ProductDetailResponse.OptionGroupDTO> optionGroups = safeList(product.getOptions()).stream()
                .filter(Objects::nonNull)
                .map(this::mapOptionGroup)
                .toList();

        List<ProductDetailResponse.VariantDTO> variantDTOs = activeVariants.stream()
                .map(this::mapVariant)
                .toList();

        List<ProductDetailResponse.ReviewDTO> reviewDTOs = reviewRepository.findByProductId(productId).stream()
                .map(this::mapReview)
                .toList();

        int totalStock = activeVariants.stream()
                .map(ProductVariant::getStockQuantity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        ProductDetailResponse response = ProductDetailResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .basePrice(product.getBasePrice())
                .imageUrls(splitImageUrls(product.getImageUrls()))

                .shopId(product.getShop() != null ? product.getShop().getShopId() : null)
                .shopName(product.getShop() != null ? product.getShop().getShopName() : null)

                .totalStock(totalStock)
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .optionGroups(optionGroups)
                .variants(variantDTOs)
                .reviews(reviewDTOs)
                .build();

        return BaseResponse.success_data("Lấy chi tiết sản phẩm thành công", response);
    }

    private boolean isSellableProduct(Product product) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            return false;
        }
        if (product.getShop() == null || product.getShop().getStatus() != ShopStatus.ACTIVE) {
            return false;
        }
        return product.getCategory() != null && product.getCategory().getCategoryStatus() == CategoryStatus.ACTIVE;
    }

    private ProductDetailResponse.OptionGroupDTO mapOptionGroup(ProductOption option) {
        List<String> values = safeList(option.getValues()).stream()
                .filter(Objects::nonNull)
                .map(ProductOptionValue::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return ProductDetailResponse.OptionGroupDTO.builder()
                .name(option.getName())
                .values(values)
                .build();
    }

    private ProductDetailResponse.VariantDTO mapVariant(ProductVariant variant) {
        Map<String, String> attributes = new HashMap<>();

        safeList(variant.getVariantOptionValues()).forEach(link -> {
            if (link == null || link.getOptionValue() == null || link.getOptionValue().getProductOption() == null) {
                return;
            }
            attributes.put(
                    link.getOptionValue().getProductOption().getName(),
                    link.getOptionValue().getValue()
            );
        });

        return ProductDetailResponse.VariantDTO.builder()
                .variantId(variant.getVariantId())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .imageUrl(variant.getImageUrl())
                .attributes(attributes)
                .build();
    }

    private ProductDetailResponse.ReviewDTO mapReview(Review review) {
        return ProductDetailResponse.ReviewDTO.builder()
                .rating(review.getRating())
                .comment(review.getComment())
                .userName(review.getUser() != null ? review.getUser().getFullName() : "Người dùng")
                .build();
    }

    private List<String> splitImageUrls(String imageUrls) {
        if (imageUrls == null || imageUrls.isBlank()) {
            return List.of();
        }
        return Arrays.stream(imageUrls.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}