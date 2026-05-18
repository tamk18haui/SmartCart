package com.gr6.SmartCart.modules.identity.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.identity.dto.ShopRegisterRequest;
import com.gr6.SmartCart.modules.identity.service.ShopRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopRegistrationController {
    private final ShopRegistrationService shopRegistrationService;

    @PostMapping("/register")
    public BaseResponse<String> registerShop(
            @Valid @RequestBody ShopRegisterRequest request
    ) {
        // Hưởng xóa userId ở đây vì thông tin User đã nằm trong request rồi
        return shopRegistrationService.registerShop(request);
    }
}