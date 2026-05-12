package com.gr6.SmartCart.module_v2.promotion.service;



import com.gr6.SmartCart.module_v2.promotion.dto.VoucherRequest;
import com.gr6.SmartCart.module_v2.promotion.dto.VoucherResponse;

import java.util.List;

public interface VoucherV2Service {
    List<VoucherResponse> getAllVouchersBySeller(String email);
    VoucherResponse createVoucher(String email, VoucherRequest request);
    VoucherResponse updateVoucher(String email, Long voucherId, VoucherRequest request);
    void deactivateVoucher(String email, Long voucherId);
}