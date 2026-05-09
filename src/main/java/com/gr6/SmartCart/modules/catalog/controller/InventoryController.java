package com.gr6.SmartCart.modules.catalog.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.catalog.dto.InventoryUpdateRequest;
import com.gr6.SmartCart.modules.catalog.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/decrease")
    public BaseResponse<String> decreaseStock(@Valid @RequestBody InventoryUpdateRequest request) {
        return inventoryService.decreaseStock(request);
    }

    // SÁNG THÊM VÀO ĐÂY: API Cộng thêm hàng vào kho an toàn
    @PostMapping("/increase")
    public BaseResponse<String> increaseStock(@Valid @RequestBody InventoryUpdateRequest request) {
        return inventoryService.increaseStock(request);
    }
}