package com.gr6.SmartCart.modules.identity.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.identity.dto.LoginRequest;
import com.gr6.SmartCart.modules.identity.service.LoginService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Quản lý xác thực")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public BaseResponse<Object> login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request);
    }
}