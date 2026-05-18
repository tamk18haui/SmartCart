package com.gr6.SmartCart.modules.identity.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.common.enums.UserStatus;
import com.gr6.SmartCart.modules.identity.dto.ShopAdminResponse;
import com.gr6.SmartCart.modules.identity.dto.UserAdminResponse;
import com.gr6.SmartCart.modules.identity.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    // --- API USERS ---
    @GetMapping("/users")
    public BaseResponse<PageResponse<UserAdminResponse>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String keyword) {
        return adminAccountService.getUsers(page, size, role, status, keyword);
    }

    @PatchMapping("/users/{userId}/ban")
    public BaseResponse<String> banUser(@PathVariable Long userId) {
        return adminAccountService.banUser(userId);
    }

    @PatchMapping("/users/{userId}/unban")
    public BaseResponse<String> unbanUser(@PathVariable Long userId) {
        return adminAccountService.unbanUser(userId);
    }

    // --- API SHOPS ---
    @GetMapping("/shops")
    public BaseResponse<PageResponse<ShopAdminResponse>> getShops(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ShopStatus status,
            @RequestParam(required = false) String keyword) {
        return adminAccountService.getShops(page, size, status, keyword);
    }

    @PatchMapping("/shops/{shopId}/approve")
    public BaseResponse<String> approveShop(@PathVariable Long shopId) {
        return adminAccountService.approveShop(shopId);
    }

    @PatchMapping("/shops/{shopId}/reject")
    public BaseResponse<String> rejectShop(@PathVariable Long shopId, @RequestParam(required = false) String reason) {
        return adminAccountService.rejectShop(shopId, reason);
    }

    @PatchMapping("/shops/{shopId}/ban")
    public BaseResponse<String> banShop(@PathVariable Long shopId, @RequestParam(required = false) String reason) {
        return adminAccountService.banShop(shopId, reason);
    }

    @PatchMapping("/shops/{shopId}/unban")
    public BaseResponse<String> unbanShop(@PathVariable Long shopId) {
        return adminAccountService.unbanShop(shopId);
    }
}