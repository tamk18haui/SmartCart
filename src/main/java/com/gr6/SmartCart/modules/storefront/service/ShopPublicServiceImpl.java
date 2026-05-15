package com.gr6.SmartCart.modules.storefront.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.modules.catalog.repository.CatalogOrderItemRepository;
import com.gr6.SmartCart.modules.catalog.repository.CatalogReviewRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.storefront.dto.ShopProductResponse;
import com.gr6.SmartCart.modules.storefront.dto.ShopPublicResponse;
import com.gr6.SmartCart.modules.storefront.repository.ShopPublicRepository;
import com.gr6.SmartCart.modules.storefront.service.ShopPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopPublicServiceImpl implements ShopPublicService {

    private final ShopPublicRepository shopPublicRepository;
    private final ProductRepository productRepository;
    private final CatalogReviewRepository catalogReviewRepository;
    private final CatalogOrderItemRepository catalogOrderItemRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<ShopPublicResponse> getShopDetail(Long shopId) {
        if (shopId == null || shopId <= 0) {
            return BaseResponse.error(400, "shopId không hợp lệ");
        }

        Shop shop = shopPublicRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop"));

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            return BaseResponse.error(404, "Shop hiện không khả dụng");
        }

        Long productCount = productRepository.countByShop_ShopIdAndStatus(
                shopId,
                ProductStatus.ACTIVE
        );

        Long voucherCount = shop.getVouchers() == null
                ? 0L
                : (long) shop.getVouchers().size();

        ShopPublicResponse response = ShopPublicResponse.builder()
                .shopId(shop.getShopId())
                .shopName(shop.getShopName())
                .description(shop.getDescription())
                .pickupAddress(shop.getPickupAddress())
                .status(shop.getStatus() == null ? null : shop.getStatus().name())
                .productCount(productCount)
                .voucherCount(voucherCount)
                .ratingAverage(5.0)
                .reviewCount(0L)
                .build();

        return BaseResponse.success_data("Lấy thông tin shop thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<ShopProductResponse>> getShopProducts(
            Long shopId,
            int page,
            int size
    ) {
        if (shopId == null || shopId <= 0) {
            return BaseResponse.error(400, "shopId không hợp lệ");
        }

        Shop shop = shopPublicRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop"));

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            return BaseResponse.error(404, "Shop hiện không khả dụng");
        }

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Page<Product> productPage = productRepository.findByShop_ShopIdAndStatusOrderByProductIdDesc(
                shopId,
                ProductStatus.ACTIVE,
                PageRequest.of(safePage - 1, safeSize)
        );

        PageResponse<ShopProductResponse> response = PageResponse.<ShopProductResponse>builder()
                .currentPage(productPage.getNumber() + 1)
                .pageSize(productPage.getSize())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .data(
                        productPage.getContent()
                                .stream()
                                .map(this::mapProduct)
                                .collect(Collectors.toList())
                )
                .build();

        return BaseResponse.success_data("Lấy sản phẩm của shop thành công", response);
    }

    private ShopProductResponse mapProduct(Product product) {
        Long productId = product.getProductId();

        Double ratingAverage = catalogReviewRepository.getAverageRatingByProductId(productId);
        Integer soldQuantity = catalogOrderItemRepository.getSoldQuantityByProductId(
                productId,
                OrderStatus.DELIVERED
        );

        return ShopProductResponse.builder()
                .productId(product.getProductId())
                .shopId(product.getShop() == null ? null : product.getShop().getShopId())
                .shopName(product.getShop() == null ? null : product.getShop().getShopName())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .price(product.getBasePrice())
                .imageUrl(getFirstImage(product.getImageUrls()))
                .soldQuantity(soldQuantity == null ? 0 : soldQuantity)
                .ratingAverage(ratingAverage == null ? 0.0 : ratingAverage)
                .reviewCount(0)
                .status(product.getStatus() == null ? null : product.getStatus().name())
                .build();
    }

    private String getFirstImage(String imageUrls) {
        if (imageUrls == null || imageUrls.trim().isEmpty()) {
            return null;
        }

        return Arrays.stream(imageUrls.split(","))
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .findFirst()
                .orElse(null);
    }
}