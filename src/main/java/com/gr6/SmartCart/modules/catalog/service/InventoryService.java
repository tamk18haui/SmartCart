package com.gr6.SmartCart.modules.catalog.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.catalog.dto.InventoryUpdateRequest;

public interface InventoryService {
    BaseResponse<String> decreaseStock(InventoryUpdateRequest request);
}