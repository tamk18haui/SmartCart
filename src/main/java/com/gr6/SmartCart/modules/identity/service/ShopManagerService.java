package com.gr6.SmartCart.modules.identity.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.identity.dto.ShopManagerRequest;
import org.springframework.transaction.annotation.Transactional;

public interface ShopManagerService {
    /**
     * Cập nhật thông tin chi tiết của Shop (Tên, mô tả, địa chỉ lấy hàng)
     */
    @Transactional
    BaseResponse updateShop(ShopManagerRequest request);
}