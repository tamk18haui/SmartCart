package com.gr6.SmartCart.modules.finance_core.service;

import com.gr6.SmartCart.modules.finance_core.dto.ShopVoucherResponse;

import java.util.List;

public interface VoucherService {

    List<ShopVoucherResponse> getShopVouchers(
            Long shopId,
            Long userId,
            Long orderValue
    );

    Long calculateDiscount(
            String code,
            Long totalAmount,
            Long shopId,
            Long userId
    );

    void useVoucher(
            String code,
            Long userId
    );
}