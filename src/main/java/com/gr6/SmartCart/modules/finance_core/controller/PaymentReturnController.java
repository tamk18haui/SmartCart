package com.gr6.SmartCart.modules.finance_core.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.enums.PaymentProvider;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCallbackRequest;
import com.gr6.SmartCart.modules.finance_core.service.OrderService;
import com.gr6.SmartCart.modules.finance_core.util.PaymentCryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
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
        return htmlResult(
                Boolean.TRUE.equals(isSuccessResponse(response)),
                "MoMo",
                response.getMessage()
        );
    }

    @PostMapping("/momo/ipn")
    public BaseResponse<?> momoIpn(@RequestBody Map<String, Object> body) {
        Map<String, String> params = new HashMap<>();

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            params.put(
                    entry.getKey(),
                    entry.getValue() == null ? "" : String.valueOf(entry.getValue())
            );
        }

        return processMomo(params);
    }

    @GetMapping("/vnpay/return")
    public ResponseEntity<String> vnpayReturn(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        boolean validSignature = verifyVnpaySignature(params, request);

        if (!validSignature) {
            return htmlResult(false, "VNPay", "Sai chữ ký VNPay");
        }

        BaseResponse<?> response = processVnpay(params);

        return htmlResult(
                Boolean.TRUE.equals(isSuccessResponse(response)),
                "VNPay",
                response.getMessage()
        );
    }

    @GetMapping("/vnpay/ipn")
    public BaseResponse<?> vnpayIpn(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        if (!verifyVnpaySignature(params, request)) {
            return BaseResponse.error(400, "Sai chữ ký VNPay");
        }

        return processVnpay(params);
    }

    private boolean verifyVnpaySignature(
            Map<String, String> params,
            HttpServletRequest request
    ) {
        if (vnpayHashSecret == null || vnpayHashSecret.trim().isEmpty()) {
            System.out.println("VNPAY VERIFY ERROR: hash secret empty");
            return false;
        }

        if (params == null || params.isEmpty()) {
            System.out.println("VNPAY VERIFY ERROR: params empty");
            return false;
        }

        String secureHash = params.get("vnp_SecureHash");

        if (secureHash == null || secureHash.trim().isEmpty()) {
            System.out.println("VNPAY VERIFY ERROR: vnp_SecureHash empty");
            return false;
        }

        String rawQuery = request.getQueryString();
        String hashData = buildVnpayHashDataFromRawQuery(rawQuery);

        String calculated = PaymentCryptoUtil.hmacSHA512(
                hashData,
                vnpayHashSecret.trim()
        );

        System.out.println("========== VNPAY VERIFY DEBUG ==========");
        System.out.println("HASH_SECRET_LENGTH = " + vnpayHashSecret.trim().length());
        System.out.println("RAW_QUERY = " + rawQuery);
        System.out.println("HASH_DATA_RETURN = " + hashData);
        System.out.println("SECURE_HASH_RETURN = " + secureHash);
        System.out.println("SECURE_HASH_CALCULATED = " + calculated);
        System.out.println("VALID = " + secureHash.trim().equalsIgnoreCase(calculated));
        System.out.println("========================================");

        return secureHash.trim().equalsIgnoreCase(calculated);
    }

    private String buildVnpayHashDataFromRawQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return "";
        }

        List<String> pairs = new ArrayList<>();

        String[] items = rawQuery.split("&");

        for (String item : items) {
            if (item == null || item.trim().isEmpty()) continue;

            String key = item;
            int equalIndex = item.indexOf("=");

            if (equalIndex >= 0) {
                key = item.substring(0, equalIndex);
            }

            if ("vnp_SecureHash".equals(key)
                    || "vnp_SecureHashType".equals(key)) {
                continue;
            }

            pairs.add(item);
        }

        pairs.sort((a, b) -> {
            String keyA = a.contains("=") ? a.substring(0, a.indexOf("=")) : a;
            String keyB = b.contains("=") ? b.substring(0, b.indexOf("=")) : b;
            return keyA.compareTo(keyB);
        });

        return String.join("&", pairs);
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
                String decoded = new String(
                        Base64.getDecoder().decode(extraData),
                        StandardCharsets.UTF_8
                );

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

        boolean success = "00".equals(responseCode)
                && "00".equals(transactionStatus);

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

    private ResponseEntity<String> htmlResult(
            boolean success,
            String provider,
            String message
    ) {
        String color = success ? "#16a34a" : "#dc2626";
        String title = success ? "Thanh toán thành công" : "Thanh toán thất bại";

        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-family: Arial, sans-serif;
                            background: #f6f7f9;
                        }
                        .card {
                            width: 92%%;
                            max-width: 420px;
                            background: white;
                            border-radius: 18px;
                            padding: 28px 20px;
                            text-align: center;
                            box-shadow: 0 10px 30px rgba(0,0,0,0.08);
                        }
                        .icon {
                            width: 64px;
                            height: 64px;
                            border-radius: 50%%;
                            margin: 0 auto 16px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            color: white;
                            font-size: 34px;
                            font-weight: bold;
                            background: %s;
                        }
                        h2 {
                            margin: 0 0 12px;
                            color: #111827;
                        }
                        p {
                            margin: 8px 0;
                            color: #4b5563;
                            line-height: 1.5;
                        }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="icon">%s</div>
                        <h2>%s</h2>
                        <p>Cổng thanh toán: <b>%s</b></p>
                        <p>%s</p>
                        <p>Bạn có thể quay lại app SmartCart và kiểm tra lịch sử đơn hàng.</p>
                    </div>
                </body>
                </html>
                """.formatted(
                title,
                color,
                success ? "✓" : "×",
                title,
                provider,
                message == null ? "" : message
        );

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}