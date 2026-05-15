package com.gr6.SmartCart.modules.finance_core.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Voucher;
import com.gr6.SmartCart.common.enums.VoucherStatus;
import com.gr6.SmartCart.modules.finance_core.dto.ShopVoucherResponse;
import com.gr6.SmartCart.modules.finance_core.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class BuyerVoucherController {

    private final VoucherRepository voucherRepository;

    @GetMapping("/shop/{shopId}")
    public BaseResponse<List<ShopVoucherResponse>> getVouchersByShop(
            @PathVariable Long shopId
    ) {
        if (shopId == null || shopId <= 0) {
            return BaseResponse.error(400, "Shop không hợp lệ");
        }

        LocalDateTime now = LocalDateTime.now();

        List<ShopVoucherResponse> vouchers = voucherRepository.findByShop_ShopId(shopId)
                .stream()
                .filter(this::isVoucherActive)
                .filter(voucher -> isVoucherInTime(voucher, now))
                .filter(this::isVoucherStillUsable)
                .map(ShopVoucherResponse::fromEntity)
                .collect(Collectors.toList());

        return BaseResponse.success_data(
                "Lấy danh sách voucher của shop thành công",
                vouchers
        );
    }

    private boolean isVoucherActive(Voucher voucher) {
        return voucher != null && voucher.getStatus() == VoucherStatus.ACTIVE;
    }

    private boolean isVoucherInTime(Voucher voucher, LocalDateTime now) {
        if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) {
            return false;
        }

        if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now)) {
            return false;
        }

        return true;
    }

    private boolean isVoucherStillUsable(Voucher voucher) {
        Integer usageLimit = voucher.getUsageLimit();
        Integer usedCount = voucher.getUsedCount();

        if (usageLimit == null || usageLimit <= 0) {
            return true;
        }

        if (usedCount == null) {
            return true;
        }

        return usedCount < usageLimit;
    }
}