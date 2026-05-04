package com.gr6.SmartCart.modules.identity.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.identity.dto.RegisterRequest;
import com.gr6.SmartCart.modules.identity.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegisterService registerService;

    @PostMapping("/register")
    public BaseResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        // Gọi sang Service để xử lý
        return registerService.register(request);
    }
}