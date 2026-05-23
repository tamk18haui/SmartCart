package com.gr6.SmartCart.modules.catalog.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.catalog.dto.InventoryItemResponse;
import com.gr6.SmartCart.modules.catalog.dto.InventoryUpdateRequest;

import java.util.List;

public interface InventoryService {

    BaseResponse<List<InventoryItemResponse>> getSellerInventory(String keyword, Integer lowStockThreshold);

    BaseResponse<String> decreaseStock(InventoryUpdateRequest request);

    BaseResponse<String> increaseStock(InventoryUpdateRequest request);
}
