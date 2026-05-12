package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
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
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.common.enums.UserStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.dto.VariantCreateRequest;
import com.gr6.SmartCart.modules.catalog.dto.VariantResponse;
import com.gr6.SmartCart.modules.catalog.repository.ProductOptionRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductOptionValueRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.catalog.repository.VariantOptionValueRepository;
import com.gr6.SmartCart.modules.catalog.service.ProductVariantService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;
    private final ProductOptionValueRepository optionValueRepository;
    private final VariantOptionValueRepository variantOptionValueRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản đang đăng nhập!"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa!");
        }
        if (user.getRole() != UserRole.SELLER) {
            throw new RuntimeException("Chỉ seller mới được quản lý biến thể sản phẩm!");
        }
        if (user.getShop() == null) {
            throw new RuntimeException("Bạn chưa đăng ký shop!");
        }
        if (user.getShop().getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop chưa hoạt động hoặc đã bị khóa!");
        }
        return user;
    }

    private void validateProductManageableByCurrentSeller(Product product) {
        User currentUser = getCurrentUser();
        Shop currentShop = currentUser.getShop();

        if (product.getShop() == null || !product.getShop().getShopId().equals(currentShop.getShopId())) {
            throw new RuntimeException("Bạn không có quyền thao tác sản phẩm này!");
        }
        if (product.getStatus() == ProductStatus.BANNED || product.getStatus() == ProductStatus.DELETED) {
            throw new RuntimeException("Sản phẩm đã khóa/xóa, không thể thao tác biến thể!");
        }
        if (product.getShop().getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop đang bị khóa hoặc chưa hoạt động!");
        }
        if (product.getCategory() == null || product.getCategory().getCategoryStatus() != CategoryStatus.ACTIVE) {
            throw new RuntimeException("Danh mục sản phẩm đang bị khóa!");
        }
    }

    private void validateSkuUniqueForCreate(String sku) {
        if (variantRepository.existsBySku(sku)) {
            throw new RuntimeException("Mã SKU này đã tồn tại trong hệ thống!");
        }
    }

    private void validateSkuUniqueForUpdate(ProductVariant variant, String newSku) {
        if (!variant.getSku().equals(newSku) && variantRepository.existsBySku(newSku)) {
            throw new RuntimeException("Mã SKU này đã có người dùng!");
        }
    }

    private void validateAttributeCombination(Product product, VariantCreateRequest request, Long ignoredVariantId) {
        if (request.getAttributes() == null || request.getAttributes().isEmpty()) {
            return;
        }

        Set<String> newOptionValues = new HashSet<>(request.getAttributes().values());
        boolean existed = product.getVariants().stream()
                .filter(v -> v.getStatus() != VariantStatus.DELETED)
                .filter(v -> ignoredVariantId == null || !v.getVariantId().equals(ignoredVariantId))
                .map(v -> v.getVariantOptionValues().stream()
                        .map(vov -> vov.getOptionValue().getValue())
                        .collect(Collectors.toSet()))
                .anyMatch(vals -> vals.equals(newOptionValues));

        if (existed) {
            throw new RuntimeException("Biến thể với tổ hợp phân loại này đã tồn tại!");
        }
    }

    @Override
    @Transactional
    public BaseResponse<VariantResponse> createVariant(VariantCreateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        validateProductManageableByCurrentSeller(product);
        validateSkuUniqueForCreate(request.getSku());
        validateAttributeCombination(product, request, null);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setImageUrl(request.getImageUrl());
        variant.setStatus(VariantStatus.ACTIVE);

        ProductVariant savedVariant = variantRepository.save(variant);
        saveVariantAttributes(product, savedVariant, request);

        return BaseResponse.success_data("Tạo biến thể thành công!", VariantResponse.fromEntity(savedVariant));
    }

    @Override
    @Transactional
    public BaseResponse<VariantResponse> updateVariant(Long variantId, VariantCreateRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

        Product product = variant.getProduct();
        validateProductManageableByCurrentSeller(product);

        if (variant.getStatus() == VariantStatus.DELETED) {
            throw new RuntimeException("Biến thể đã bị xóa, không thể cập nhật!");
        }

        validateSkuUniqueForUpdate(variant, request.getSku());
        validateAttributeCombination(product, request, variantId);

        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setImageUrl(request.getImageUrl());
        variant.setStatus(VariantStatus.ACTIVE);

        ProductVariant savedVariant = variantRepository.save(variant);
        return BaseResponse.success_data("Cập nhật biến thể thành công!", VariantResponse.fromEntity(savedVariant));
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteVariant(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

        Product product = variant.getProduct();
        validateProductManageableByCurrentSeller(product);

        long activeVariants = product.getVariants().stream()
                .filter(v -> v.getStatus() == VariantStatus.ACTIVE)
                .count();

        if (activeVariants <= 1) {
            throw new RuntimeException("Không thể xóa phân loại cuối cùng!");
        }

        variant.setStatus(VariantStatus.DELETED);
        variantRepository.save(variant);

        return BaseResponse.successMessage("Xóa biến thể thành công!");
    }

    private void saveVariantAttributes(Product product, ProductVariant savedVariant, VariantCreateRequest request) {
        if (request.getAttributes() == null || request.getAttributes().isEmpty()) {
            return;
        }

        request.getAttributes().forEach((optionName, valueName) -> {
            ProductOption option = product.getOptions().stream()
                    .filter(o -> o.getName().equals(optionName))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductOption newOption = new ProductOption();
                        newOption.setName(optionName);
                        newOption.setProduct(product);
                        newOption.setValues(new java.util.ArrayList<>()); // Khởi tạo mảng tránh lỗi Null
                        return optionRepository.save(newOption);
                    });

            ProductOptionValue optionValue = option.getValues().stream()
                    .filter(v -> v.getValue().equals(valueName))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductOptionValue newValue = new ProductOptionValue();
                        newValue.setValue(valueName);
                        newValue.setProductOption(option);
                        return optionValueRepository.save(newValue);
                    });

            VariantOptionValue link = new VariantOptionValue();
            link.setVariant(savedVariant);
            link.setOptionValue(optionValue);
            variantOptionValueRepository.save(link);
        });
    }
}