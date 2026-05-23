package com.gr6.SmartCart.modules.finance_core.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.enums.PaymentProvider;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCallbackRequest;
import com.gr6.SmartCart.modules.finance_core.service.OrderService;
import com.gr6.SmartCart.modules.finance_core.util.PaymentCryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentReturnController {

    private final OrderService orderService;

    @Value("${payment.momo.secret-key:0C062880131E8BB3604D3D3223FDEAA4}")
    private String momoSecretKey;

    @Value("${payment.vnpay.hash-secret:}")
    private String vnpayHashSecret;

    @GetMapping("/momo/return")
    public ResponseEntity<String> momoReturn(@RequestParam Map<String, String> params) {
        BaseResponse<?> response = processMomo(params);
        return htmlResult(Boolean.TRUE.equals(isSuccessResponse(response)), "MoMo", response.getMessage());
    }

    @PostMapping("/momo/ipn")
    public BaseResponse<?> momoIpn(@RequestBody Map<String, Object> body) {
        Map<String, String> params = new HashMap<>();

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            params.put(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }

        return processMomo(params);
    }

    private BaseResponse<?> processMomo(Map<String, String> params) {
        String extraData = params.get("extraData");
        String orderIdRaw = params.get("orderId");
        String transId = params.get("transId");
        String resultCode = params.get("resultCode");

        Long orderId = null;
        Long transactionId = null;

        try {
            if (extraData != null && !extraData.trim().isEmpty()) {
                String decoded = new String(Base64.getDecoder().decode(extraData), StandardCharsets.UTF_8);
                Map<String, String> extra = parseQueryLike(decoded);

                orderId = Long.parseLong(extra.get("orderId"));
                transactionId = Long.parseLong(extra.get("transactionId"));
            }
        } catch (Exception ignored) {
        }

        if (orderId == null || transactionId == null) {
            long[] ids = parseSmartCartOrderId(orderIdRaw);
            orderId = ids[0];
            transactionId = ids[1];
        }

        PaymentCallbackRequest request = new PaymentCallbackRequest();
        request.setOrderId(orderId);
        request.setTransactionId(transactionId);
        request.setPaymentProvider(PaymentProvider.MOMO);
        request.setProviderTransactionId(transId == null ? orderIdRaw : transId);
        request.setSuccess("0".equals(resultCode));
        request.setSignature("MOMO_TEST_CALLBACK");

        return orderService.handlePaymentCallback(request);
    }

    @GetMapping("/vnpay/return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
        boolean validSignature = verifyVnpaySignature(params);

        if (!validSignature) {
            return htmlResult(false, "VNPay", "Sai chữ ký VNPay");
        }

        BaseResponse<?> response = processVnpay(params);
        return htmlResult(Boolean.TRUE.equals(isSuccessResponse(response)), "VNPay", response.getMessage());
    }

    @GetMapping("/vnpay/ipn")
    public BaseResponse<?> vnpayIpn(@RequestParam Map<String, String> params) {
        if (!verifyVnpaySignature(params)) {
            return BaseResponse.error(400, "Sai chữ ký VNPay");
        }

        return processVnpay(params);
    }

    private BaseResponse<?> processVnpay(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        String transactionNo = params.get("vnp_TransactionNo");

        if (txnRef == null || !txnRef.contains("-")) {
            return BaseResponse.error(400, "Mã giao dịch VNPay không hợp lệ");
        }

        Long orderId;
        Long transactionId;

        try {
            String[] parts = txnRef.split("-");

            if (parts.length < 2) {
                return BaseResponse.error(400, "Mã giao dịch VNPay không hợp lệ");
            }

            orderId = Long.parseLong(parts[0]);
            transactionId = Long.parseLong(parts[1]);
        } catch (Exception e) {
            return BaseResponse.error(400, "Không đọc được mã đơn hàng từ VNPay");
        }

        boolean success = "00".equals(responseCode) && "00".equals(transactionStatus);

        PaymentCallbackRequest request = new PaymentCallbackRequest();
        request.setOrderId(orderId);
        request.setTransactionId(transactionId);
        request.setPaymentProvider(PaymentProvider.VNPAY);
        request.setProviderTransactionId(
                transactionNo == null || transactionNo.trim().isEmpty()
                        ? txnRef
                        : transactionNo
        );
        request.setSuccess(success);
        request.setSignature("VNPAY_TEST_CALLBACK");

        return orderService.handlePaymentCallback(request);
    }
    private boolean verifyVnpaySignature(Map<String, String> params) {
        if (vnpayHashSecret == null || vnpayHashSecret.trim().isEmpty()) {
            return false;
        }

        if (params == null || params.isEmpty()) {
            return false;
        }

        String secureHash = params.get("vnp_SecureHash");

        if (secureHash == null || secureHash.trim().isEmpty()) {
            return false;
        }

        Map<String, String> data = new HashMap<>(params);
        data.remove("vnp_SecureHash");
        data.remove("vnp_SecureHashType");

        String hashData = PaymentCryptoUtil.buildQuery(data);
        String calculated = PaymentCryptoUtil.hmacSHA512(
                hashData,
                vnpayHashSecret.trim()
        );

        return secureHash.trim().equalsIgnoreCase(calculated);
    }
    private Map<String, String> parseQueryLike(String raw) {
        Map<String, String> map = new HashMap<>();

        if (raw == null || raw.trim().isEmpty()) {
            return map;
        }

        String[] pairs = raw.split("&");

        for (String pair : pairs) {
            if (!pair.contains("=")) continue;

            String[] kv = pair.split("=", 2);
            map.put(kv[0], kv.length > 1 ? kv[1] : "");
        }

        return map;
    }

    private long[] parseSmartCartOrderId(String raw) {
        if (raw == null) return new long[]{0L, 0L};

        // Format: SC{orderId}TX{transactionId}
        try {
            String value = raw.replace("SC", "");
            String[] parts = value.split("TX");

            return new long[]{
                    Long.parseLong(parts[0]),
                    Long.parseLong(parts[1])
            };
        } catch (Exception e) {
            return new long[]{0L, 0L};
        }
    }

    private Boolean isSuccessResponse(BaseResponse<?> response) {
        return response != null && response.getStatus() == 200;
    }

    private ResponseEntity<String> htmlResult(boolean success, String provider, String message) {
        String color = success ? "#16a34a" : "#dc2626";
        String title = success ? "Thanh toán thành công" : "Thanh toán thất bại";

        String html = "<!doctype html><html><head><meta charset='UTF-8'/>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'/>"
                + "<title>" + title + "</title>"
                + "</head><body style='font-family:Arial;background:#f6f7fb;padding:24px'>"
                + "<div style='max-width:460px;margin:40px auto;background:white;border-radius:20px;padding:24px;text-align:center;box-shadow:0 8px 24px rgba(0,0,0,.08)'>"
                + "<div style='font-size:48px;color:" + color + "'>" + (success ? "✓" : "×") + "</div>"
                + "<h2 style='color:#111827'>" + title + "</h2>"
                + "<p style='color:#6b7280'>Cổng thanh toán: <b>" + provider + "</b></p>"
                + "<p style='color:#6b7280'>" + message + "</p>"
                + "<p style='margin-top:20px;color:#9ca3af;font-size:13px'>Bạn có thể quay lại app SmartCart và kiểm tra lịch sử đơn hàng.</p>"
                + "</div></body></html>";

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}