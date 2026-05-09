package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.modules.catalog.dto.InventoryUpdateRequest;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.catalog.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public BaseResponse<String> decreaseStock(InventoryUpdateRequest request) {
        ProductVariant variant = productVariantRepository.findByIdWithLock(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

        if (variant.getStockQuantity() < request.getQuantity()) {
            return BaseResponse.error(400, "Số lượng tồn kho không đủ!");
        }

        variant.setStockQuantity(variant.getStockQuantity() - request.getQuantity());
        productVariantRepository.save(variant);
        return BaseResponse.successMessage("Đã trừ kho thành công!");
    }

    // SÁNG THÊM VÀO ĐÂY: Logic cộng thêm hàng có khóa Lock an toàn
    @Override
    @Transactional
    public BaseResponse<String> increaseStock(InventoryUpdateRequest request) {
        ProductVariant variant = productVariantRepository.findByIdWithLock(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

        variant.setStockQuantity(variant.getStockQuantity() + request.getQuantity());
        productVariantRepository.save(variant);

        return BaseResponse.successMessage("Đã cộng thêm " + request.getQuantity() + " sản phẩm vào kho!");
    }
}