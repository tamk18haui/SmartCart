package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.domain.Category;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductOption;
import com.gr6.SmartCart.common.domain.ProductOptionValue;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.domain.VariantOptionValue;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.dto.ProductRequest;
import com.gr6.SmartCart.modules.catalog.dto.ProductResponse;
import com.gr6.SmartCart.modules.catalog.dto.ProductVariantRequest;
import com.gr6.SmartCart.modules.catalog.repository.CategoryRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductOptionRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductOptionValueRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.catalog.repository.VariantOptionValueRepository;
import com.gr6.SmartCart.modules.catalog.service.ProductService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final int MAX_BRAND_SUGGESTIONS = 30;

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductOptionRepository optionRepository;
    private final ProductOptionValueRepository optionValueRepository;
    private final VariantOptionValueRepository variantOptionValueRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BaseResponse<ProductResponse> createProduct(ProductRequest request) {
        Shop shop = getCurrentActiveShop();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        if (category.getCategoryStatus() != CategoryStatus.ACTIVE) {
            throw new RuntimeException("Danh mục này đang bị ẩn, không thể đăng sản phẩm!");
        }

        List<ProductVariantRequest> validVariants = normalizeVariantRequests(request.getVariants());

        if (validVariants.isEmpty() && request.getStockQuantity() == null) {
            throw new RuntimeException("Vui lòng nhập tồn kho hoặc thêm biến thể sản phẩm!");
        }

        if (!validVariants.isEmpty()) {
            validateDuplicateVariantCombinations(validVariants);
        }

        Product product = new Product();
        product.setShop(shop);
        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(trimToNull(request.getDescription()));
        product.setBrand(trimToNull(request.getBrand()));
        product.setCondition(request.getCondition());
        product.setBasePrice(request.getBasePrice());
        product.setWeight(request.getWeight());
        product.setLength(request.getLength());
        product.setWidth(request.getWidth());
        product.setHeight(request.getHeight());
        product.setStatus(ProductStatus.ACTIVE);
        product.setImageUrls(joinUrls(request.getUploadImages()));

        Product savedProduct = productRepository.save(product);

        if (validVariants.isEmpty()) {
            createDefaultVariant(savedProduct, request.getStockQuantity());
        } else {
            createProductVariants(savedProduct, validVariants);
        }

        Product result = productRepository
                .findByProductIdAndShopShopIdAndStatusNot(
                        savedProduct.getProductId(),
                        shop.getShopId(),
                        ProductStatus.DELETED
                )
                .orElse(savedProduct);

        return BaseResponse.success_data(
                "Tạo sản phẩm thành công",
                ProductResponse.fromEntity(result)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<ProductResponse>> getProductsByShop(Long shopId, int page, int size) {
        Page<Product> products = productRepository.findByShopShopIdAndStatusNot(
                shopId,
                ProductStatus.DELETED,
                PageRequest.of(Math.max(page - 1, 0), normalizeSize(size))
        );

        Page<ProductResponse> responsePage = products.map(ProductResponse::fromEntity);

        return BaseResponse.success(PageResponse.of(responsePage));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<ProductResponse> getProductForSeller(Long productId) {
        Shop shop = getCurrentActiveShop();

        Product product = productRepository
                .findByProductIdAndShopShopIdAndStatusNot(
                        productId,
                        shop.getShopId(),
                        ProductStatus.DELETED
                )
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc bạn không có quyền xem!"));

        return BaseResponse.success(ProductResponse.fromEntity(product));
    }

    @Override
    @Transactional
    public BaseResponse<ProductResponse> updateProduct(Long productId, ProductRequest request) {
        Shop shop = getCurrentActiveShop();

        Product product = productRepository
                .findByProductIdAndShopShopIdAndStatusNot(
                        productId,
                        shop.getShopId(),
                        ProductStatus.DELETED
                )
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc bạn không có quyền sửa!"));

        if (product.getStatus() == ProductStatus.BANNED) {
            throw new RuntimeException("Sản phẩm đang bị admin khóa, seller không thể tự sửa!");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        if (category.getCategoryStatus() != CategoryStatus.ACTIVE) {
            throw new RuntimeException("Danh mục này đang bị ẩn!");
        }

        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(trimToNull(request.getDescription()));
        product.setBrand(trimToNull(request.getBrand()));
        product.setCondition(request.getCondition());
        product.setBasePrice(request.getBasePrice());
        product.setWeight(request.getWeight());
        product.setLength(request.getLength());
        product.setWidth(request.getWidth());
        product.setHeight(request.getHeight());

        if (request.getUploadImages() != null) {
            product.setImageUrls(joinUrls(request.getUploadImages()));
        }

        Product savedProduct = productRepository.save(product);

        return BaseResponse.success_data(
                "Cập nhật sản phẩm thành công",
                ProductResponse.fromEntity(savedProduct)
        );
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteProduct(Long productId) {
        Shop shop = getCurrentActiveShop();

        Product product = productRepository
                .findByProductIdAndShopShopIdAndStatusNot(
                        productId,
                        shop.getShopId(),
                        ProductStatus.DELETED
                )
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc bạn không có quyền xóa!"));

        if (product.getStatus() == ProductStatus.BANNED) {
            throw new RuntimeException("Sản phẩm đang bị admin khóa, seller không thể tự xóa!");
        }

        product.setStatus(ProductStatus.DELETED);

        if (product.getVariants() != null) {
            product.getVariants().forEach(variant -> variant.setStatus(VariantStatus.DELETED));
        }

        productRepository.save(product);

        return BaseResponse.successMessage("Xóa sản phẩm thành công");
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<String>> getBrandSuggestions(String keyword) {
        List<String> brands = productRepository.searchDistinctBrands(
                normalizeKeyword(keyword),
                PageRequest.of(0, MAX_BRAND_SUGGESTIONS)
        );

        return BaseResponse.success_data("Lấy danh sách thương hiệu thành công", brands);
    }

    private Shop getCurrentActiveShop() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        Shop shop = user.getShop();

        if (shop == null) {
            throw new RuntimeException("Tài khoản hiện tại chưa đăng ký shop!");
        }

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop chưa được duyệt hoặc đã bị khóa!");
        }

        return shop;
    }

    private void createDefaultVariant(Product product, Integer stockQuantity) {
        ProductVariant defaultVariant = new ProductVariant();
        defaultVariant.setProduct(product);
        defaultVariant.setSku("P-" + product.getProductId() + "-DEFAULT");
        defaultVariant.setPrice(product.getBasePrice());
        defaultVariant.setStockQuantity(stockQuantity == null ? 0 : stockQuantity);
        defaultVariant.setImageUrl(firstImage(product.getImageUrls()));
        defaultVariant.setIsDefault(true);
        defaultVariant.setStatus(VariantStatus.ACTIVE);

        variantRepository.save(defaultVariant);
    }

    private void createProductVariants(Product product, List<ProductVariantRequest> variants) {
        int index = 1;

        for (ProductVariantRequest request : variants) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(normalizeSku(request.getSku(), product.getProductId(), index));
            variant.setPrice(defaultPrice(request.getPrice(), product.getBasePrice()));
            variant.setStockQuantity(request.getStockQuantity() == null ? 0 : request.getStockQuantity());
            variant.setImageUrl(trimToNull(request.getImageUrl()));
            variant.setIsDefault(index == 1);
            variant.setStatus(VariantStatus.ACTIVE);

            ProductVariant savedVariant = variantRepository.save(variant);

            saveVariantAttributes(savedVariant, normalizeAttributes(request.getAttributes()));

            index++;
        }
    }

    private void saveVariantAttributes(ProductVariant variant, Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            ProductOption option = optionRepository
                    .findByProductProductIdAndNameIgnoreCase(
                            variant.getProduct().getProductId(),
                            entry.getKey()
                    )
                    .orElseGet(() -> {
                        ProductOption newOption = new ProductOption();
                        newOption.setProduct(variant.getProduct());
                        newOption.setName(entry.getKey());
                        return optionRepository.save(newOption);
                    });

            ProductOptionValue optionValue = optionValueRepository
                    .findByProductOptionProductOptionIdAndValueIgnoreCase(
                            option.getProductOptionId(),
                            entry.getValue()
                    )
                    .orElseGet(() -> {
                        ProductOptionValue newValue = new ProductOptionValue();
                        newValue.setProductOption(option);
                        newValue.setValue(entry.getValue());
                        return optionValueRepository.save(newValue);
                    });

            VariantOptionValue link = new VariantOptionValue();
            link.setVariant(variant);
            link.setOptionValue(optionValue);

            variantOptionValueRepository.save(link);
        }
    }

    private void validateDuplicateVariantCombinations(List<ProductVariantRequest> variants) {
        Set<String> seen = new HashSet<>();

        for (ProductVariantRequest variant : variants) {
            Map<String, String> attrs = normalizeAttributes(variant.getAttributes());

            String key = attrs.entrySet()
                    .stream()
                    .map(entry -> normalizeCompare(entry.getKey()) + "=" + normalizeCompare(entry.getValue()))
                    .sorted()
                    .collect(Collectors.joining("|"));

            if (key.isBlank()) {
                key = normalizeCompare(variant.getSku());
            }

            if (!seen.add(key)) {
                throw new RuntimeException("Có biến thể bị trùng tổ hợp phân loại!");
            }
        }
    }

    private List<ProductVariantRequest> normalizeVariantRequests(List<ProductVariantRequest> variants) {
        if (variants == null) {
            return List.of();
        }

        return variants.stream()
                .filter(Objects::nonNull)
                .filter(variant ->
                        variant.getAttributes() != null && !variant.getAttributes().isEmpty()
                                || variant.getSku() != null && !variant.getSku().isBlank()
                )
                .collect(Collectors.toList());
    }

    private Map<String, String> normalizeAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }

        Map<String, String> normalized = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            String key = entry.getKey().trim();
            String value = entry.getValue().trim();

            if (!key.isBlank() && !value.isBlank()) {
                normalized.put(key, value);
            }
        }

        return normalized;
    }

    private String joinUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return null;
        }

        String joined = urls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(","));

        return joined.isBlank() ? null : joined;
    }

    private String firstImage(String imageUrls) {
        if (imageUrls == null || imageUrls.isBlank()) {
            return null;
        }

        String[] parts = imageUrls.split(",");

        if (parts.length == 0) {
            return null;
        }

        return parts[0].trim();
    }

    private BigDecimal defaultPrice(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private String normalizeSku(String sku, Long productId, int index) {
        String safeSku = trimToNull(sku);

        if (safeSku != null) {
            return safeSku;
        }

        return "P-" + productId + "-VAR-" + index;
    }

    private String normalizeKeyword(String keyword) {
        return trimToNull(keyword);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isBlank() ? null : trimmed;
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 10;
        }

        return Math.min(size, 100);
    }

    private String normalizeCompare(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(
                value.trim().toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD
        );

        return normalized.replaceAll("\\p{M}", "");
    }
}