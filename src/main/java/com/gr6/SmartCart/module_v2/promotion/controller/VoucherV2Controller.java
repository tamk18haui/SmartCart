package com.gr6.SmartCart.module_v2.promotion.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.module_v2.promotion.dto.VoucherRequest;
import com.gr6.SmartCart.module_v2.promotion.dto.VoucherResponse;
import com.gr6.SmartCart.module_v2.promotion.service.VoucherV2Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/seller/vouchers")
@RequiredArgsConstructor
public class VoucherV2Controller {

    private final VoucherV2Service voucherService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<VoucherResponse>>> getAllVouchers(Authentication authentication) {
        String email = authentication.getName(); // Lấy email từ Token JWT
        List<VoucherResponse> vouchers = voucherService.getAllVouchersBySeller(email);
        return ResponseEntity.ok(BaseResponse.success_data("Lấy danh sách voucher thành công", vouchers));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<VoucherResponse>> createVoucher(
            Authentication authentication,
            @Valid @RequestBody VoucherRequest request) {
        String email = authentication.getName();
        VoucherResponse response = voucherService.createVoucher(email, request);
        return ResponseEntity.ok(BaseResponse.success_data("Tạo voucher thành công", response));
    }

    @PutMapping("/{voucherId}")
    public ResponseEntity<BaseResponse<VoucherResponse>> updateVoucher(
            Authentication authentication,
            @PathVariable Long voucherId,
            @Valid @RequestBody VoucherRequest request) {
        String email = authentication.getName();
        VoucherResponse response = voucherService.updateVoucher(email, voucherId, request);
        return ResponseEntity.ok(BaseResponse.success_data("Cập nhật voucher thành công", response));
    }

    @DeleteMapping("/{voucherId}")
    public ResponseEntity<BaseResponse<Object>> deactivateVoucher(
            Authentication authentication,
            @PathVariable Long voucherId) {
        String email = authentication.getName();
        voucherService.deactivateVoucher(email, voucherId);
        // Giả sử BaseResponse có hàm success (không data), nếu không bạn có thể trả về null ở phần data
        return ResponseEntity.ok(BaseResponse.success_data("Đã ngừng kích hoạt voucher thành công", null));
    }
}