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
    @Transactional // Đảm bảo tính toàn vẹn: Nếu có lỗi xảy ra, mọi thay đổi sẽ được khôi phục (rollback)
    public BaseResponse<String> decreaseStock(InventoryUpdateRequest request) {
        // 1. Kiểm tra sản phẩm có tồn tại trong hệ thống không
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm có ID: " + request.getVariantId()));

        // 2. Logic kiểm tra tồn kho
        if (variant.getStockQuantity() < request.getQuantity()) {
            return BaseResponse.error(400, "Lỗi: Số lượng tồn kho không đủ! (Hiện còn: " + variant.getStockQuantity() + ")");
        }

        // 3. Thực hiện trừ kho
        variant.setStockQuantity(variant.getStockQuantity() - request.getQuantity());

        // 4. Cập nhật lại Database
        productVariantRepository.save(variant);

        return BaseResponse.successMessage("Đã trừ kho thành công cho biến thể ID: " + request.getVariantId());
    }
}