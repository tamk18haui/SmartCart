package com.gr6.SmartCart.module_v3.withdraw.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.module_v3.withdraw.dto.*;
import com.gr6.SmartCart.module_v3.withdraw.service.SellerWithdrawService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/seller/withdraw")
@RequiredArgsConstructor
public class SellerWithdrawController {

    private final SellerWithdrawService sellerWithdrawService;

    @GetMapping("/wallet")
    public BaseResponse<WalletSummaryResponse> getMyWallet() {
        return sellerWithdrawService.getMyWallet();
    }

    @GetMapping("/wallet/transactions")
    public BaseResponse<PageResponse<WalletTransactionResponse>> getMyWalletTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return sellerWithdrawService.getMyWalletTransactions(page, size);
    }

    @PostMapping("/requests")
    public BaseResponse<WithdrawResponse> createWithdrawRequest(
            @Valid @RequestBody WithdrawCreateRequest request
    ) {
        return sellerWithdrawService.createWithdrawRequest(request);
    }

    @GetMapping("/requests")
    public BaseResponse<PageResponse<WithdrawResponse>> getMyWithdrawRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return sellerWithdrawService.getMyWithdrawRequests(page, size);
    }

    @GetMapping("/settlements")
    public BaseResponse<PageResponse<SellerSettlementResponse>> getMySettlements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return sellerWithdrawService.getMySettlements(page, size);
    }
}