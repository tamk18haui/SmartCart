package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.*;
import com.gr6.SmartCart.modules.catalog.dto.*;
import com.gr6.SmartCart.modules.catalog.repository.*;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.catalog.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductOptionRepository optionRepository;
    private final ProductOptionValueRepository optionValueRepository;
    private final VariantOptionValueRepository variantOptionValueRepository;
    private final UserRepository userRepository;

    private Shop getCurrentShop() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        if (user.getShop() == null) {
            throw new RuntimeException("Bạn chưa đăng ký mở Shop!");
        }
        return user.getShop();
    }

    @Override
    @Transactional
    public BaseResponse<VariantResponse> createVariant(VariantCreateRequest request) {
        if (variantRepository.existsBySku(request.getSku())) {
            return BaseResponse.error(400, "Mã vạch (SKU) này đã tồn tại, vui lòng đổi mã khác!");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm gốc!"));

        Shop currentShop = getCurrentShop();
        if (!product.getShop().getShopId().equals(currentShop.getShopId())) {
            return BaseResponse.error(403, "Cảnh báo: Không thể can thiệp vào sản phẩm của cửa hàng khác!");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setImageUrl(request.getImageUrl());
        ProductVariant savedVariant = variantRepository.save(variant);

        if (request.getAttributes() != null) {
            request.getAttributes().forEach((optionName, valueName) -> {
                ProductOption option = product.getOptions().stream()
                        .filter(o -> o.getName().equals(optionName))
                        .findFirst().orElseGet(() -> {
                            ProductOption newOpt = new ProductOption();
                            newOpt.setName(optionName);
                            newOpt.setProduct(product);
                            return optionRepository.save(newOpt);
                        });

                ProductOptionValue optValue = option.getValues().stream()
                        .filter(v -> v.getValue().equals(valueName))
                        .findFirst().orElseGet(() -> {
                            ProductOptionValue newVal = new ProductOptionValue();
                            newVal.setValue(valueName);
                            newVal.setProductOption(option);
                            return optionValueRepository.save(newVal);
                        });

                VariantOptionValue link = new VariantOptionValue();
                link.setVariant(savedVariant);
                link.setOptionValue(optValue);
                variantOptionValueRepository.save(link);
            });
        }

        return BaseResponse.success_data("Tạo biến thể thành công!", VariantResponse.fromEntity(savedVariant));
    }

    @Override
    @Transactional
    public BaseResponse<VariantResponse> updateVariant(Long variantId, VariantCreateRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

        Shop currentShop = getCurrentShop();
        if (!variant.getProduct().getShop().getShopId().equals(currentShop.getShopId())) {
            return BaseResponse.error(403, "Cảnh báo: Bạn không có quyền sửa biến thể này!");
        }

        if (!variant.getSku().equals(request.getSku()) && variantRepository.existsBySku(request.getSku())) {
            return BaseResponse.error(400, "Mã SKU này đã có người dùng!");
        }

        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setImageUrl(request.getImageUrl());

        variantRepository.save(variant);
        return BaseResponse.success_data("Cập nhật biến thể thành công!", VariantResponse.fromEntity(variant));
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteVariant(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

        Shop currentShop = getCurrentShop();
        if (!variant.getProduct().getShop().getShopId().equals(currentShop.getShopId())) {
            return BaseResponse.error(403, "Cảnh báo: Bạn không có quyền xóa biến thể này!");
        }

        // CHỐT CHẶN: Dùng Try-Catch để bắt lỗi Constraint Violation nếu biến thể đã có trong Đơn hàng
        try {
            variantRepository.deleteById(variantId);
            return BaseResponse.successMessage("Xóa biến thể thành công!");
        } catch (Exception e) {
            return BaseResponse.error(400, "Không thể xóa do biến thể này đã phát sinh giao dịch. Vui lòng cập nhật tồn kho về 0 thay vì xóa!");
        }
    }
}