package com.gr6.SmartCart.module_v3.withdraw.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.enums.WithdrawStatus;
import com.gr6.SmartCart.module_v3.withdraw.dto.*;
import com.gr6.SmartCart.module_v3.withdraw.service.AdminWithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/admin/withdraw")
@RequiredArgsConstructor
public class AdminWithdrawController {

    private final AdminWithdrawService adminWithdrawService;

    @PostMapping("/reconcile")
    public BaseResponse<ReconcileResponse> reconcileCompletedOrders() {
        return adminWithdrawService.reconcileCompletedOrders();
    }

    @GetMapping("/settlements")
    public BaseResponse<PageResponse<SellerSettlementResponse>> getSettlements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminWithdrawService.getSettlements(page, size);
    }

    @GetMapping("/requests")
    public BaseResponse<PageResponse<WithdrawResponse>> getWithdrawRequests(
            @RequestParam(required = false) WithdrawStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminWithdrawService.getWithdrawRequests(status, page, size);
    }

    @PatchMapping("/requests/{withdrawId}/approve")
    public BaseResponse<WithdrawResponse> approveWithdraw(
            @PathVariable Long withdrawId,
            @RequestBody(required = false) AdminWithdrawDecisionRequest request
    ) {
        return adminWithdrawService.approveWithdraw(withdrawId, request);
    }

    @PatchMapping("/requests/{withdrawId}/reject")
    public BaseResponse<WithdrawResponse> rejectWithdraw(
            @PathVariable Long withdrawId,
            @RequestBody(required = false) AdminWithdrawDecisionRequest request
    ) {
        return adminWithdrawService.rejectWithdraw(withdrawId, request);
    }
}