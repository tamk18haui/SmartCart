package com.gr6.SmartCart.module_v3.withdraw.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.module_v3.withdraw.dto.*;

public interface SellerWithdrawService {

    BaseResponse<WalletSummaryResponse> getMyWallet();

    BaseResponse<PageResponse<WalletTransactionResponse>> getMyWalletTransactions(int page, int size);

    BaseResponse<WithdrawResponse> createWithdrawRequest(WithdrawCreateRequest request);

    BaseResponse<PageResponse<WithdrawResponse>> getMyWithdrawRequests(int page, int size);

    BaseResponse<PageResponse<SellerSettlementResponse>> getMySettlements(int page, int size);
}