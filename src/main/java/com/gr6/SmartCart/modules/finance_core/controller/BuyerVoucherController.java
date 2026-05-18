package com.gr6.SmartCart.modules.finance_core.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.modules.finance_core.dto.ShopVoucherResponse;
import com.gr6.SmartCart.modules.finance_core.service.VoucherService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class BuyerVoucherController {

    private final VoucherService voucherService;
    private final UserRepository userRepository;

    @GetMapping("/shop/{shopId}")
    public BaseResponse<List<ShopVoucherResponse>> getVouchersByShop(
            @PathVariable Long shopId,
            @RequestParam(required = false) Long orderValue,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        List<ShopVoucherResponse> vouchers = voucherService.getShopVouchers(
                shopId,
                userId,
                orderValue
        );

        return BaseResponse.success_data(
                "Lấy voucher của shop thành công",
                vouchers
        );
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElse(null);
    }
}