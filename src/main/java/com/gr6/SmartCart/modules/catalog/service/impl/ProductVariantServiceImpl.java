package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.*;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.dto.VariantCreateRequest;
import com.gr6.SmartCart.modules.catalog.dto.VariantResponse;
import com.gr6.SmartCart.modules.catalog.repository.*;
import com.gr6.SmartCart.modules.catalog.service.ProductVariantService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductOptionRepository optionRepository;
    private final ProductOptionValueRepository optionValueRepository;
    private final VariantOptionValueRepository variantOptionValueRepository;
    private final UserRepository userRepository;

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

    @Override
    @Transactional
    public BaseResponse<VariantResponse> createVariant(VariantCreateRequest request) {
        Shop shop = getCurrentActiveShop();

        Product product = productRepository
                .findByProductIdAndShopShopIdAndStatusNot(
                        request.getProductId(),
                        shop.getShopId(),
                        ProductStatus.DELETED
                )
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc bạn không có quyền thêm biến thể!"));

        if (product.getStatus() == ProductStatus.BANNED) {
            throw new RuntimeException("Sản phẩm đang bị admin khóa, không thể thêm biến thể!");
        }

        if (variantRepository.existsByProductProductIdAndSku(product.getProductId(), request.getSku().trim())) {
            throw new RuntimeException("SKU đã tồn tại trong sản phẩm này!");
        }

        Map<String, String> normalizedAttributes = normalizeAttributes(request.getAttributes());

        if (!normalizedAttributes.isEmpty() && hasDuplicateCombination(product, normalizedAttributes, null)) {
            throw new RuntimeException("Tổ hợp phân loại này đã tồn tại!");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.getSku().trim());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setImageUrl(request.getImageUrl());
        variant.setIsDefault(false);
        variant.setStatus(VariantStatus.ACTIVE);

        ProductVariant savedVariant = variantRepository.save(variant);

        saveVariantAttributes(savedVariant, normalizedAttributes);

        ProductVariant result = variantRepository.findById(savedVariant.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không thể tải lại biến thể vừa tạo!"));

        return BaseResponse.success_data(
                "Tạo biến thể thành công",
                VariantResponse.fromEntity(result)
        );
    }

    @Override
    @Transactional
    public BaseResponse<VariantResponse> updateVariant(Long variantId, VariantCreateRequest request) {
        Shop shop = getCurrentActiveShop();

        ProductVariant variant = variantRepository
                .findByVariantIdAndProductShopShopIdAndStatusNot(
                        variantId,
                        shop.getShopId(),
                        VariantStatus.DELETED
                )
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể hoặc bạn không có quyền sửa!"));

        Product product = variant.getProduct();

        if (product.getStatus() == ProductStatus.BANNED) {
            throw new RuntimeException("Sản phẩm đang bị admin khóa, không thể sửa biến thể!");
        }

        if (!Objects.equals(product.getProductId(), request.getProductId())) {
            throw new RuntimeException("Không được chuyển biến thể sang sản phẩm khác!");
        }

        if (variantRepository.existsByProductProductIdAndSkuAndVariantIdNot(
                product.getProductId(),
                request.getSku().trim(),
                variantId
        )) {
            throw new RuntimeException("SKU đã tồn tại trong sản phẩm này!");
        }

        Map<String, String> normalizedAttributes = normalizeAttributes(request.getAttributes());

        if (!normalizedAttributes.isEmpty() && hasDuplicateCombination(product, normalizedAttributes, variantId)) {
            throw new RuntimeException("Tổ hợp phân loại này đã tồn tại!");
        }

        variant.setSku(request.getSku().trim());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setImageUrl(request.getImageUrl());

        variantOptionValueRepository.deleteByVariantVariantId(variantId);
        variantRepository.save(variant);

        saveVariantAttributes(variant, normalizedAttributes);

        ProductVariant result = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không thể tải lại biến thể vừa cập nhật!"));

        return BaseResponse.success_data(
                "Cập nhật biến thể thành công",
                VariantResponse.fromEntity(result)
        );
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteVariant(Long variantId) {
        Shop shop = getCurrentActiveShop();

        ProductVariant variant = variantRepository
                .findByVariantIdAndProductShopShopIdAndStatusNot(
                        variantId,
                        shop.getShopId(),
                        VariantStatus.DELETED
                )
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể hoặc bạn không có quyền xóa!"));

        if (Boolean.TRUE.equals(variant.getIsDefault())) {
            long activeVariantCount = variantRepository
                    .findByProductProductIdAndStatusNot(
                            variant.getProduct().getProductId(),
                            VariantStatus.DELETED
                    )
                    .size();

            if (activeVariantCount <= 1) {
                throw new RuntimeException("Không thể xóa biến thể mặc định cuối cùng của sản phẩm!");
            }
        }

        variant.setStatus(VariantStatus.DELETED);
        variantRepository.save(variant);

        return BaseResponse.successMessage("Xóa biến thể thành công");
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

            VariantOptionValue variantOptionValue = new VariantOptionValue();
            variantOptionValue.setVariant(variant);
            variantOptionValue.setOptionValue(optionValue);

            variantOptionValueRepository.save(variantOptionValue);
        }
    }

    private boolean hasDuplicateCombination(
            Product product,
            Map<String, String> attributes,
            Long ignoredVariantId
    ) {
        return variantRepository
                .findByProductProductIdAndStatusNot(product.getProductId(), VariantStatus.DELETED)
                .stream()
                .filter(variant -> ignoredVariantId == null || !Objects.equals(variant.getVariantId(), ignoredVariantId))
                .map(this::toAttributeMap)
                .anyMatch(existing -> existing.equals(attributes));
    }

    private Map<String, String> toAttributeMap(ProductVariant variant) {
        if (variant.getVariantOptionValues() == null) {
            return Map.of();
        }

        return variant.getVariantOptionValues()
                .stream()
                .filter(vov -> vov.getOptionValue() != null && vov.getOptionValue().getProductOption() != null)
                .collect(Collectors.toMap(
                        vov -> normalize(vov.getOptionValue().getProductOption().getName()),
                        vov -> normalize(vov.getOptionValue().getValue()),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    private Map<String, String> normalizeAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }

        return attributes.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .filter(entry -> !entry.getKey().isBlank() && !entry.getValue().isBlank())
                .collect(Collectors.toMap(
                        entry -> normalize(entry.getKey()),
                        entry -> normalize(entry.getValue()),
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new
                ));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}