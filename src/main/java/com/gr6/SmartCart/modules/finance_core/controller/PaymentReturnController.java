package com.gr6.SmartCart.modules.finance_core.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Order;
import com.gr6.SmartCart.common.domain.ShopOrder;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.PaymentProvider;
import com.gr6.SmartCart.common.enums.PaymentStatus;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCallbackRequest;
import com.gr6.SmartCart.modules.finance_core.repository.OrderRepository;
import com.gr6.SmartCart.modules.finance_core.repository.ShopOrderRepository;
import com.gr6.SmartCart.modules.finance_core.service.OrderService;
import com.gr6.SmartCart.modules.finance_core.util.PaymentCryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentReturnController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ShopOrderRepository shopOrderRepository;

    @Value("${payment.momo.access-key:F8BBA842F85A0C39174B4F842B7B0FB5}")
    private String momoAccessKey;

    @Value("${payment.momo.secret-key:0C062880131E8BB3604D3D3223FDEAA4}")
    private String momoSecretKey;

    @Value("${payment.vnpay.hash-secret:}")
    private String vnpayHashSecret;

    @GetMapping(
            value = "/momo/return",
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> momoReturn(
            @RequestParam Map<String, String> params
    ) {
        String momoOrderId = params.get("orderId");

        Long orderId = extractOrderIdFromMomoOrderId(momoOrderId);

        System.out.println("========== MOMO RETURN ==========");
        System.out.println("params = " + params);
        System.out.println("momoOrderId = " + momoOrderId);
        System.out.println("orderId = " + orderId);
        System.out.println("resultCode = " + params.get("resultCode"));
        System.out.println("message = " + params.get("message"));
        System.out.println("=================================");

        String resultCode = params.get("resultCode");

        BaseResponse<?> response;

        if ("0".equals(resultCode)) {
            response = processMomo(params);
        } else {
            /*
             * MoMo đã trả resultCode khác 0.
             * Không được đánh dấu đơn là đã thanh toán.
             */
            response = processMomo(params);
        }

        Order order = findOrder(orderId);

        boolean paidInDatabase = order != null
                && order.getPaymentStatus() == PaymentStatus.COMPLETED
                && order.getStatus() == OrderStatus.PENDING;

        boolean success = paidInDatabase
                || (response != null && response.getStatus() == 200 && "0".equals(resultCode));

        String message;

        if (success) {
            message = "Thanh toán MoMo thành công";
        } else if (params.get("message") != null && !params.get("message").isBlank()) {
            message = params.get("message");
        } else {
            message = "Thanh toán MoMo thất bại";
        }

        return htmlRedirectToApp(
                success,
                orderId,
                findFirstShopOrderId(orderId),
                findTotalAmount(orderId),
                "ONLINE",
                "MOMO",
                success ? "COMPLETED" : "FAILED",
                success ? "PENDING" : "PAYMENT_FAILED",
                message
        );
    }

    private Order findOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            return null;
        }

        return orderRepository.findById(orderId).orElse(null);
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<Void> momoIpn(
            @RequestBody Map<String, Object> body
    ) {
        Map<String, String> params = convertToStringMap(body);

        if (!verifyMomoSignature(params)) {
            System.out.println("MOMO IPN: invalid signature");
            return ResponseEntity.noContent().build();
        }

        processMomo(params);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(
            value = "/vnpay/return",
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> vnpayReturn(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        Long orderId = extractOrderIdFromVnpTxnRef(params.get("vnp_TxnRef"));

        boolean validSignature = verifyVnpaySignature(request);

        if (!validSignature) {
            return htmlRedirectToApp(
                    false,
                    orderId,
                    findFirstShopOrderId(orderId),
                    findTotalAmount(orderId),
                    "ONLINE",
                    "VNPAY",
                    "FAILED",
                    "PAYMENT_FAILED",
                    "Sai chữ ký VNPay"
            );
        }

        BaseResponse<?> response = processVnpay(params);

        boolean success = response != null && response.getStatus() == 200;

        return htmlRedirectToApp(
                success,
                orderId,
                findFirstShopOrderId(orderId),
                findTotalAmount(orderId),
                "ONLINE",
                "VNPAY",
                success ? "COMPLETED" : "FAILED",
                success ? "PENDING" : "PAYMENT_FAILED",
                response == null ? "Không xử lý được thanh toán VNPay" : response.getMessage()
        );
    }

    @GetMapping("/vnpay/ipn")
    public Map<String, String> vnpayIpn(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        if (!verifyVnpaySignature(request)) {
            return vnpayResponse("97", "Checksum failed");
        }

        BaseResponse<?> response = processVnpay(params);

        if (response == null) {
            return vnpayResponse("99", "Unknown error");
        }

        if (response.getStatus() == 200) {
            return vnpayResponse("00", "Success");
        }

        return vnpayResponse("99", response.getMessage());
    }

    private BaseResponse<?> processMomo(Map<String, String> params) {
        String momoOrderId = params.get("orderId");
        String resultCodeRaw = params.get("resultCode");
        String transId = params.get("transId");

        Long orderId = extractOrderIdFromMomoOrderId(momoOrderId);
        Long transactionId = extractTransactionIdFromMomoOrderId(momoOrderId);

        if (orderId == null || transactionId == null) {
            return BaseResponse.error(400, "Mã giao dịch MoMo không hợp lệ");
        }

        int resultCode;

        try {
            resultCode = Integer.parseInt(resultCodeRaw == null ? "-1" : resultCodeRaw);
        } catch (Exception e) {
            resultCode = -1;
        }

        boolean success = resultCode == 0;

        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest();
        callbackRequest.setOrderId(orderId);
        callbackRequest.setTransactionId(transactionId);
        callbackRequest.setPaymentProvider(PaymentProvider.MOMO);
        callbackRequest.setProviderTransactionId(
                transId == null || transId.trim().isEmpty()
                        ? momoOrderId
                        : transId
        );
        callbackRequest.setSuccess(success);
        callbackRequest.setSignature("MOMO_VERIFIED_CALLBACK");

        System.out.println("========== MOMO CALLBACK ==========");
        System.out.println("momoOrderId = " + momoOrderId);
        System.out.println("orderId = " + orderId);
        System.out.println("transactionId = " + transactionId);
        System.out.println("resultCode = " + resultCode);
        System.out.println("transId = " + transId);
        System.out.println("success = " + success);
        System.out.println("===================================");

        return orderService.handlePaymentCallback(callbackRequest);
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

            orderId = Long.parseLong(parts[0]);
            transactionId = Long.parseLong(parts[1]);

        } catch (Exception e) {
            return BaseResponse.error(400, "Không đọc được mã đơn hàng từ VNPay");
        }

        boolean success = "00".equals(responseCode)
                && "00".equals(transactionStatus);

        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest();
        callbackRequest.setOrderId(orderId);
        callbackRequest.setTransactionId(transactionId);
        callbackRequest.setPaymentProvider(PaymentProvider.VNPAY);
        callbackRequest.setProviderTransactionId(
                transactionNo == null || transactionNo.trim().isEmpty()
                        ? txnRef
                        : transactionNo
        );
        callbackRequest.setSuccess(success);
        callbackRequest.setSignature("VNPAY_VERIFIED_CALLBACK");

        System.out.println("========== VNPAY CALLBACK ==========");
        System.out.println("txnRef = " + txnRef);
        System.out.println("orderId = " + orderId);
        System.out.println("transactionId = " + transactionId);
        System.out.println("vnp_ResponseCode = " + responseCode);
        System.out.println("vnp_TransactionStatus = " + transactionStatus);
        System.out.println("vnp_TransactionNo = " + transactionNo);
        System.out.println("success = " + success);
        System.out.println("====================================");

        return orderService.handlePaymentCallback(callbackRequest);
    }

    private boolean verifyMomoSignature(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return false;
        }

        if (momoAccessKey == null || momoAccessKey.trim().isEmpty()
                || momoSecretKey == null || momoSecretKey.trim().isEmpty()) {
            System.out.println("MOMO VERIFY: accessKey/secretKey empty");
            return false;
        }

        String signature = params.get("signature");

        if (signature == null || signature.trim().isEmpty()) {
            System.out.println("MOMO VERIFY: missing signature");
            return false;
        }

        String rawSignature =
                "accessKey=" + momoAccessKey.trim()
                        + "&amount=" + safeParam(params, "amount")
                        + "&extraData=" + safeParam(params, "extraData")
                        + "&message=" + safeParam(params, "message")
                        + "&orderId=" + safeParam(params, "orderId")
                        + "&orderInfo=" + safeParam(params, "orderInfo")
                        + "&orderType=" + safeParam(params, "orderType")
                        + "&partnerCode=" + safeParam(params, "partnerCode")
                        + "&payType=" + safeParam(params, "payType")
                        + "&requestId=" + safeParam(params, "requestId")
                        + "&responseTime=" + safeParam(params, "responseTime")
                        + "&resultCode=" + safeParam(params, "resultCode")
                        + "&transId=" + safeParam(params, "transId");

        String calculated = PaymentCryptoUtil.hmacSHA256(
                rawSignature,
                momoSecretKey.trim()
        );

        boolean valid = signature.trim().equalsIgnoreCase(calculated);

        System.out.println("========== MOMO VERIFY ==========");
        System.out.println("rawSignature = " + rawSignature);
        System.out.println("signature = " + signature);
        System.out.println("calculated = " + calculated);
        System.out.println("valid = " + valid);
        System.out.println("=================================");

        return valid;
    }

    private boolean verifyVnpaySignature(HttpServletRequest request) {
        if (vnpayHashSecret == null || vnpayHashSecret.trim().isEmpty()) {
            System.out.println("VNPAY VERIFY: hash secret empty");
            return false;
        }

        String rawQuery = request.getQueryString();

        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            System.out.println("VNPAY VERIFY: raw query empty");
            return false;
        }

        Map<String, String> rawMap = parseRawQuery(rawQuery);

        String secureHash = rawMap.get("vnp_SecureHash");

        if (secureHash == null || secureHash.trim().isEmpty()) {
            System.out.println("VNPAY VERIFY: secure hash empty");
            return false;
        }

        String hashData = buildVnpayHashDataFromRawQuery(rawQuery);

        String calculated = PaymentCryptoUtil.hmacSHA512(
                hashData,
                vnpayHashSecret.trim()
        );

        boolean valid = secureHash.trim().equalsIgnoreCase(calculated);

        System.out.println("========== VNPAY VERIFY ==========");
        System.out.println("hashData = " + hashData);
        System.out.println("secureHash = " + secureHash);
        System.out.println("calculated = " + calculated);
        System.out.println("valid = " + valid);
        System.out.println("==================================");

        return valid;
    }

    private String buildVnpayHashDataFromRawQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return "";
        }

        List<String> pairs = new ArrayList<>();

        String[] items = rawQuery.split("&");

        for (String item : items) {
            if (item == null || item.trim().isEmpty()) {
                continue;
            }

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

    private Map<String, String> parseRawQuery(String rawQuery) {
        Map<String, String> result = new HashMap<>();

        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return result;
        }

        String[] pairs = rawQuery.split("&");

        for (String pair : pairs) {
            if (pair == null || pair.trim().isEmpty()) {
                continue;
            }

            int index = pair.indexOf("=");

            if (index < 0) {
                result.put(pair, "");
            } else {
                result.put(
                        pair.substring(0, index),
                        pair.substring(index + 1)
                );
            }
        }

        return result;
    }

    private ResponseEntity<String> htmlRedirectToApp(
            boolean success,
            Long orderId,
            Long shopOrderId,
            long totalAmount,
            String paymentMethod,
            String paymentProvider,
            String paymentStatus,
            String orderStatus,
            String message
    ) {
        String deepLink = buildAppDeepLink(
                success,
                orderId,
                shopOrderId,
                totalAmount,
                paymentMethod,
                paymentProvider,
                paymentStatus,
                orderStatus,
                message
        );

        String title = success ? "Thanh toán thành công" : "Thanh toán thất bại";
        String icon = success ? "✅" : "❌";
        String color = success ? "#16a34a" : "#dc2626";

        String html = """
                <!doctype html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <script>
                        setTimeout(function() {
                            window.location.href = "%s";
                        }, 600);
                    </script>
                    <style>
                        body {
                            margin: 0;
                            font-family: Arial, sans-serif;
                            background: #f6f7f9;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            min-height: 100vh;
                        }
                        .card {
                            width: calc(100%% - 40px);
                            max-width: 420px;
                            background: #ffffff;
                            border-radius: 18px;
                            padding: 28px 22px;
                            text-align: center;
                            box-shadow: 0 12px 30px rgba(0,0,0,0.08);
                        }
                        .icon {
                            font-size: 54px;
                            margin-bottom: 14px;
                        }
                        h2 {
                            margin: 0 0 12px;
                            color: %s;
                            font-size: 24px;
                        }
                        p {
                            margin: 8px 0;
                            color: #444;
                            line-height: 1.5;
                        }
                        a {
                            display: inline-block;
                            margin-top: 18px;
                            padding: 12px 18px;
                            border-radius: 12px;
                            background: #3154d4;
                            color: white;
                            text-decoration: none;
                            font-weight: bold;
                        }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="icon">%s</div>
                        <h2>%s</h2>
                        <p>%s</p>
                        <p>Đang chuyển về ứng dụng SmartCart...</p>
                        <a href="%s">Mở SmartCart</a>
                    </div>
                </body>
                </html>
                """.formatted(
                title,
                deepLink,
                color,
                icon,
                title,
                message == null ? "" : message,
                deepLink
        );

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    private String buildAppDeepLink(
            boolean success,
            Long orderId,
            Long shopOrderId,
            long totalAmount,
            String paymentMethod,
            String paymentProvider,
            String paymentStatus,
            String orderStatus,
            String message
    ) {
        StringBuilder builder = new StringBuilder("smartcart://payment-result");

        builder.append("?success=").append(success);

        if (orderId != null) {
            builder.append("&orderId=").append(orderId);
        }

        if (shopOrderId != null) {
            builder.append("&shopOrderId=").append(shopOrderId);
        }

        builder.append("&totalAmount=").append(totalAmount);
        builder.append("&paymentMethod=").append(urlEncode(paymentMethod));
        builder.append("&paymentProvider=").append(urlEncode(paymentProvider));
        builder.append("&provider=").append(urlEncode(paymentProvider));
        builder.append("&paymentStatus=").append(urlEncode(paymentStatus));
        builder.append("&orderStatus=").append(urlEncode(orderStatus));
        builder.append("&message=").append(urlEncode(message));

        return builder.toString();
    }

    private String urlEncode(String value) {
        if (value == null) {
            return "";
        }

        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String safeParam(Map<String, String> params, String key) {
        String value = params.get(key);
        return value == null ? "" : value;
    }

    private Map<String, String> convertToStringMap(Map<String, Object> body) {
        Map<String, String> result = new HashMap<>();

        if (body == null) {
            return result;
        }

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            result.put(
                    entry.getKey(),
                    entry.getValue() == null ? "" : String.valueOf(entry.getValue())
            );
        }

        return result;
    }

    private Long extractOrderIdFromMomoOrderId(String momoOrderId) {
        if (momoOrderId == null) {
            return null;
        }

        try {
            String value = momoOrderId.trim();

            int scIndex = value.indexOf("SC");
            int txIndex = value.indexOf("TX");

            if (scIndex < 0 || txIndex < 0 || txIndex <= scIndex + 2) {
                return null;
            }

            return Long.parseLong(value.substring(scIndex + 2, txIndex));
        } catch (Exception e) {
            return null;
        }
    }

    private Long extractTransactionIdFromMomoOrderId(String momoOrderId) {
        if (momoOrderId == null) {
            return null;
        }

        try {
            String value = momoOrderId.trim();
            int txIndex = value.indexOf("TX");

            if (txIndex < 0 || txIndex + 2 >= value.length()) {
                return null;
            }

            return Long.parseLong(value.substring(txIndex + 2));
        } catch (Exception e) {
            return null;
        }
    }

    private Long extractOrderIdFromVnpTxnRef(String txnRef) {
        if (txnRef == null || !txnRef.contains("-")) {
            return null;
        }

        try {
            String[] parts = txnRef.split("-");
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private Long findFirstShopOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            return null;
        }

        try {
            List<ShopOrder> shopOrders = shopOrderRepository.findByOrder_OrderId(orderId);

            if (shopOrders == null || shopOrders.isEmpty()) {
                return null;
            }

            for (ShopOrder shopOrder : shopOrders) {
                if (shopOrder != null && shopOrder.getShopOrderId() != null) {
                    return shopOrder.getShopOrderId();
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private long findTotalAmount(Long orderId) {
        if (orderId == null || orderId <= 0) {
            return 0L;
        }

        try {
            Order order = orderRepository.findById(orderId).orElse(null);

            if (order == null || order.getTotalAmount() == null) {
                return 0L;
            }

            return order.getTotalAmount().longValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    private Map<String, String> vnpayResponse(String rspCode, String message) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("RspCode", rspCode);
        result.put("Message", message);
        return result;
    }
}