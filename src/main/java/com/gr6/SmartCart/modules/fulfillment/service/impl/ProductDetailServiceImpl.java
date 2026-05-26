package com.gr6.SmartCart.modules.fulfillment.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductOptionValue;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Review;
import com.gr6.SmartCart.common.domain.VariantOptionValue;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.module_v3.recommendation.event.RecommendationEventService;
import com.gr6.SmartCart.modules.catalog.repository.CatalogOrderItemRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.fulfillment.dto.ProductDetailResponse;
import com.gr6.SmartCart.modules.fulfillment.repository.ReviewRepository;
import com.gr6.SmartCart.modules.fulfillment.service.ProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductDetailServiceImpl implements ProductDetailService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final CatalogOrderItemRepository catalogOrderItemRepository;
    private final ObjectMapper objectMapper;
    private final RecommendationEventService recommendationEventService;

    /**
     * Lấy chi tiết sản phẩm cho buyer.
     *
     * Bao gồm:
     * - Thông tin sản phẩm
     * - Shop
     * - Biến thể
     * - Option/phân loại
     * - Tồn kho
     * - Số lượng đã bán
     * - Rating trung bình
     * - Danh sách review có 4 ảnh + 1 video
     */
    @Override
    @Transactional(readOnly = true)
    public BaseResponse<ProductDetailResponse> getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (!isSellableProduct(product)) {
            return BaseResponse.error(404, "Sản phẩm hiện không khả dụng.");
        }

        List<ProductVariant> activeVariants = safeList(product.getVariants())
                .stream()
                .filter(Objects::nonNull)
                .filter(variant -> variant.getStatus() == null || variant.getStatus() == VariantStatus.ACTIVE)
                .collect(Collectors.toList());

        if (activeVariants.isEmpty()) {
            return BaseResponse.error(404, "Sản phẩm hiện không còn phân loại khả dụng.");
        }

        List<ProductDetailResponse.OptionGroupDTO> optionGroups =
                buildOptionGroupsFromActiveVariants(activeVariants);

        List<ProductDetailResponse.VariantDTO> variantDTOs = activeVariants.stream()
                .map(this::mapVariant)
                .collect(Collectors.toList());

        List<Review> reviews = reviewRepository.findByProductId(productId);

        List<ProductDetailResponse.ReviewDTO> reviewDTOs = safeList(reviews)
                .stream()
                .map(this::mapReview)
                .collect(Collectors.toList());

        int totalStock = activeVariants.stream()
                .map(ProductVariant::getStockQuantity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int soldQuantity = safeInteger(catalogOrderItemRepository.getSoldQuantityByProductId(
                productId,
                List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED)
        ));

        double averageRating = calculateAverageRating(reviews);
        int reviewCount = reviews == null ? 0 : reviews.size();

        ProductDetailResponse response = ProductDetailResponse.builder()
                .productId(product.getProductId())

                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)

                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .basePrice(product.getBasePrice())
                .imageUrls(splitImageUrls(product.getImageUrls()))

                .shopId(product.getShop() != null ? product.getShop().getShopId() : null)
                .shopOwnerId(
                        product.getShop() != null && product.getShop().getUser() != null
                                ? product.getShop().getUser().getUserId()
                                : null
                )
                .shopName(product.getShop() != null ? product.getShop().getShopName() : null)
                .shopImageUrl(product.getShop().getLogoUrl())
                .shopStatus(
                        product.getShop() != null && product.getShop().getStatus() != null
                                ? product.getShop().getStatus().name()
                                : null
                )
                .totalStock(totalStock)
                .soldQuantity(soldQuantity)
                .totalSold(soldQuantity)
                .averageRating(averageRating)
                .reviewCount(reviewCount)

                .status(product.getStatus() != null ? product.getStatus().name() : null)

                .optionGroups(optionGroups)
                .variants(variantDTOs)
                .reviews(reviewDTOs)
                .build();
        recordViewProductIfLoggedIn(product);
        return BaseResponse.success_data("Lấy chi tiết sản phẩm thành công", response);
    }

    /**
     * Chỉ cho xem sản phẩm đang ACTIVE,
     * shop ACTIVE,
     * category ACTIVE.
     */
    private boolean isSellableProduct(Product product) {
        if (product == null || product.getStatus() != ProductStatus.ACTIVE) {
            return false;
        }

        if (product.getShop() == null || product.getShop().getStatus() != ShopStatus.ACTIVE) {
            return false;
        }

        return product.getCategory() != null
                && product.getCategory().getCategoryStatus() == CategoryStatus.ACTIVE;
    }

    /**
     * Tạo danh sách nhóm phân loại từ các variant đang active.
     *
     * Ví dụ:
     * - Màu sắc: Đen, Trắng
     * - Size: M, L, XL
     */
    private List<ProductDetailResponse.OptionGroupDTO> buildOptionGroupsFromActiveVariants(
            List<ProductVariant> activeVariants
    ) {
        Map<String, LinkedHashSet<String>> optionMap = new LinkedHashMap<>();

        for (ProductVariant variant : activeVariants) {
            for (VariantOptionValue link : safeList(variant.getVariantOptionValues())) {
                if (link == null || link.getOptionValue() == null) {
                    continue;
                }

                ProductOptionValue optionValue = link.getOptionValue();

                if (optionValue.getProductOption() == null) {
                    continue;
                }

                String optionName = optionValue.getProductOption().getName();
                String value = optionValue.getValue();

                if (isBlank(optionName) || isBlank(value)) {
                    continue;
                }

                optionMap
                        .computeIfAbsent(optionName.trim(), key -> new LinkedHashSet<>())
                        .add(value.trim());
            }
        }

        List<ProductDetailResponse.OptionGroupDTO> result = new ArrayList<>();

        for (Map.Entry<String, LinkedHashSet<String>> entry : optionMap.entrySet()) {
            result.add(ProductDetailResponse.OptionGroupDTO.builder()
                    .name(entry.getKey())
                    .values(new ArrayList<>(entry.getValue()))
                    .build());
        }

        return result;
    }

    /**
     * Map ProductVariant sang DTO.
     *
     * Lưu ý:
     * Không được dùng biến review trong hàm này.
     * Variant chỉ có imageUrl riêng của variant, không có imageUrls/videoUrl của review.
     */
    private ProductDetailResponse.VariantDTO mapVariant(ProductVariant variant) {
        return ProductDetailResponse.VariantDTO.builder()
                .variantId(variant.getVariantId())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .imageUrl(variant.getImageUrl())
                .isDefault(variant.getIsDefault())
                .status(variant.getStatus() != null ? variant.getStatus().name() : null)
                .attributes(buildVariantAttributes(variant))
                .build();
    }

    /**
     * Build map thuộc tính của variant.
     *
     * Ví dụ:
     * {
     *   "Màu sắc": "Đen",
     *   "Size": "XL"
     * }
     */
    private Map<String, String> buildVariantAttributes(ProductVariant variant) {
        Map<String, String> attrs = new LinkedHashMap<>();

        for (VariantOptionValue link : safeList(variant.getVariantOptionValues())) {
            if (link == null || link.getOptionValue() == null) {
                continue;
            }

            ProductOptionValue optionValue = link.getOptionValue();

            if (optionValue.getProductOption() == null) {
                continue;
            }

            String optionName = optionValue.getProductOption().getName();
            String value = optionValue.getValue();

            if (isBlank(optionName) || isBlank(value)) {
                continue;
            }

            attrs.put(optionName.trim(), value.trim());
        }

        return attrs;
    }

    /**
     * Map Review entity sang ReviewDTO hiển thị ở chi tiết sản phẩm.
     *
     * Review mới hỗ trợ:
     * - imageUrls: tối đa 4 ảnh, lưu DB dạng JSON string
     * - videoUrl: 1 video
     */
    private ProductDetailResponse.ReviewDTO mapReview(Review review) {
        return ProductDetailResponse.ReviewDTO.builder()
                .reviewId(review.getReviewId())
                .rating(review.getRating())
                .comment(review.getComment())
                .imageUrls(parseReviewImageUrls(review.getImageUrls()))
                .videoUrl(review.getVideoUrl())
                .videoThumbnailUrl(buildVideoThumbnailUrl(review.getVideoUrl()))
                .userName(review.getUser() != null ? review.getUser().getFullName() : "Người dùng SmartCart")
                .sellerReply(review.getSellerReply())
                .createdAt(review.getCreatedAt())
                .repliedAt(review.getRepliedAt())
                .build();
    }
    /**
     * Tách chuỗi ảnh sản phẩm.
     *
     * Product.imageUrls hiện đang lưu dạng:
     * url1,url2,url3
     */
    private List<String> splitImageUrls(String imageUrls) {
        if (isBlank(imageUrls)) {
            return List.of();
        }

        return Arrays.stream(imageUrls.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Tính rating trung bình từ danh sách review.
     */
    private double calculateAverageRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        return reviews.stream()
                .filter(Objects::nonNull)
                .map(Review::getRating)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Đọc JSON string imageUrls từ Review.
     *
     * DB lưu:
     * ["url1","url2","url3","url4"]
     *
     * API trả:
     * List<String>
     */
    private List<String> parseReviewImageUrls(String rawJson) {
        try {
            if (rawJson == null || rawJson.trim().isEmpty()) {
                return List.of();
            }

            return objectMapper.readValue(
                    rawJson,
                    new TypeReference<List<String>>() {
                    }
            );
        } catch (Exception e) {
            return List.of();
        }
    }
    private void recordViewProductIfLoggedIn(Product product) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return;
            }

            String email = authentication.getName();

            if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
                return;
            }

            recommendationEventService.recordViewProductByEmail(email, product);

        } catch (Exception ignored) {
        }
    }

    private String buildVideoThumbnailUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return null;
        }

        String url = videoUrl.trim();

        if (url.contains("/video/upload/")) {
            String thumbnail = url.replace("/video/upload/", "/video/upload/so_0/");
            int queryIndex = thumbnail.indexOf("?");

            String query = "";

            if (queryIndex >= 0) {
                query = thumbnail.substring(queryIndex);
                thumbnail = thumbnail.substring(0, queryIndex);
            }

            int dotIndex = thumbnail.lastIndexOf(".");

            if (dotIndex > thumbnail.lastIndexOf("/")) {
                thumbnail = thumbnail.substring(0, dotIndex) + ".jpg";
            } else {
                thumbnail = thumbnail + ".jpg";
            }

            return thumbnail + query;
        }

        return null;
    }
    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}