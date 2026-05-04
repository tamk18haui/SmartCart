package com.gr6.SmartCart.modules.identity.service;

import com.gr6.SmartCart.modules.identity.dto.ShopManagerRequest;

public interface ShopManagerService {
    /**
     * Cập nhật thông tin chi tiết của Shop (Tên, mô tả, địa chỉ lấy hàng)
     */
    String updateShop(Integer id, ShopManagerRequest request);
}