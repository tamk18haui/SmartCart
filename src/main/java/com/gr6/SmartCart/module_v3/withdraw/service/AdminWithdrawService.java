package com.gr6.SmartCart.module_v3.withdraw.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.enums.WithdrawStatus;
import com.gr6.SmartCart.module_v3.withdraw.dto.*;

public interface AdminWithdrawService {

    BaseResponse<ReconcileResponse> reconcileCompletedOrders();

    BaseResponse<PageResponse<SellerSettlementResponse>> getSettlements(int page, int size);

    BaseResponse<PageResponse<WithdrawResponse>> getWithdrawRequests(
            WithdrawStatus status,
            int page,
            int size
    );

    BaseResponse<WithdrawResponse> approveWithdraw(
            Long withdrawId,
            AdminWithdrawDecisionRequest request
    );

    BaseResponse<WithdrawResponse> rejectWithdraw(
            Long withdrawId,
            AdminWithdrawDecisionRequest request
    );
}