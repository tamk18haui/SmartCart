package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.*;
import com.gr6.SmartCart.modules.catalog.dto.VariantCreateRequest;
import com.gr6.SmartCart.modules.catalog.repository.*;
import com.gr6.SmartCart.modules.catalog.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductOptionRepository optionRepository;
    private final ProductOptionValueRepository optionValueRepository;
    private final VariantOptionValueRepository variantOptionValueRepository;

    @Override
    @Transactional
    public BaseResponse<ProductVariant> createVariant(VariantCreateRequest request) {
        // 1. Tìm sản phẩm gốc
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm gốc!"));

        // 2. Tạo thực thể Variant (Biến thể)
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setImageUrl(request.getImageUrl());
        ProductVariant savedVariant = variantRepository.save(variant);

        // 3. Xử lý các thuộc tính (Màu sắc, Size...)
        request.getAttributes().forEach((optionName, valueName) -> {
            // Tìm hoặc tạo Option (vd: Màu sắc)
            ProductOption option = product.getOptions().stream()
                    .filter(o -> o.getName().equals(optionName))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductOption newOpt = new ProductOption();
                        newOpt.setName(optionName);
                        newOpt.setProduct(product);
                        return optionRepository.save(newOpt);
                    });

            // Tìm hoặc tạo Value (vd: Đỏ)
            ProductOptionValue optValue = option.getValues().stream()
                    .filter(v -> v.getValue().equals(valueName))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductOptionValue newVal = new ProductOptionValue();
                        newVal.setValue(valueName);
                        newVal.setProductOption(option);
                        return optionValueRepository.save(newVal);
                    });

            // Gắn link Biến thể với Giá trị thuộc tính
            VariantOptionValue link = new VariantOptionValue();
            link.setVariant(savedVariant);
            link.setOptionValue(optValue);
            variantOptionValueRepository.save(link);
        });

        return BaseResponse.success_data("Tạo biến thể thành công!", savedVariant);
    }
}