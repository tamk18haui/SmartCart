package com.gr6.SmartCart.modules.finance_core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gr6.SmartCart.common.domain.Order;
import com.gr6.SmartCart.common.domain.Transaction;
import com.gr6.SmartCart.common.enums.PaymentProvider;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCreateResult;
import com.gr6.SmartCart.modules.finance_core.service.PaymentGatewayService;
import com.gr6.SmartCart.modules.finance_core.util.PaymentCryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DevPaymentGatewayServiceImpl implements PaymentGatewayService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Value("${payment.momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String momoEndpoint;

    @Value("${payment.momo.partner-code:MOMOBKUN20180529}")
    private String momoPartnerCode;

    @Value("${payment.momo.access-key:F8BBA842F85A0C39174B4F842B7B0FB5}")
    private String momoAccessKey;

    @Value("${payment.momo.secret-key:0C062880131E8BB3604D3D3223FDEAA4}")
    private String momoSecretKey;

    @Value("${payment.momo.request-type:captureWallet}")
    private String momoRequestType;

    @Value("${payment.vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpayUrl;

    @Value("${payment.vnpay.tmn-code:}")
    private String vnpayTmnCode;

    @Value("${payment.vnpay.hash-secret:}")
    private String vnpayHashSecret;

    @Override
    public PaymentCreateResult createPaymentUrl(
            Order order,
            Transaction transaction,
            PaymentProvider provider
    ) {
        if (provider == null || provider == PaymentProvider.NONE) {
            throw new RuntimeException("Vui lòng chọn cổng thanh toán MOMO hoặc VNPAY");
        }

        if (provider == PaymentProvider.MOMO) {
            return createMomoPaymentUrl(order, transaction);
        }

        if (provider == PaymentProvider.VNPAY) {
            return createVnpayPaymentUrl(order, transaction);
        }

        throw new RuntimeException("Cổng thanh toán không hợp lệ");
    }

    private PaymentCreateResult createMomoPaymentUrl(Order order, Transaction transaction) {
        String requestId = "SC-" + order.getOrderId()
                + "-" + transaction.getTransactionId()
                + "-" + System.currentTimeMillis();

        String orderId = "SC" + order.getOrderId()
                + "TX" + transaction.getTransactionId();

        String amount = String.valueOf(transaction.getAmount());

        String orderInfo = "Thanh toan don hang SmartCart #" + order.getOrderId();

        String redirectUrl = publicBaseUrl + "/api/v1/payments/momo/return";
        String ipnUrl = publicBaseUrl + "/api/v1/payments/momo/ipn";

        // Để rỗng cho khớp với raw data MoMo đang báo
        String extraData = "";

        String rawSignature =
                "accessKey=" + momoAccessKey +
                        "&amount=" + amount +
                        "&extraData=" + extraData +
                        "&ipnUrl=" + ipnUrl +
                        "&orderId=" + orderId +
                        "&orderInfo=" + orderInfo +
                        "&partnerCode=" + momoPartnerCode +
                        "&redirectUrl=" + redirectUrl +
                        "&requestId=" + requestId +
                        "&requestType=" + momoRequestType;

        String signature = PaymentCryptoUtil.hmacSHA256(rawSignature, momoSecretKey);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", momoPartnerCode);
        body.put("accessKey", momoAccessKey);
        body.put("partnerName", "SmartCart");
        body.put("storeId", "SmartCartStore");
        body.put("requestId", requestId);
        body.put("amount", amount);
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("lang", "vi");
        body.put("requestType", momoRequestType);
        body.put("autoCapture", true);
        body.put("extraData", extraData);
        body.put("signature", signature);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    momoEndpoint,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());

            int resultCode = root.path("resultCode").asInt(-1);

            if (resultCode != 0) {
                String message = root.path("message").asText("Tạo thanh toán MoMo thất bại");
                throw new RuntimeException("MoMo lỗi: " + message);
            }

            String payUrl = root.path("payUrl").asText(null);

            if (payUrl == null || payUrl.trim().isEmpty()) {
                payUrl = root.path("deeplink").asText(null);
            }

            if (payUrl == null || payUrl.trim().isEmpty()) {
                throw new RuntimeException("MoMo không trả paymentUrl");
            }

            return PaymentCreateResult.builder()
                    .paymentUrl(payUrl)
                    .providerTransactionId(orderId)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Không tạo được thanh toán MoMo: " + e.getMessage());
        }
    }
    private PaymentCreateResult createVnpayPaymentUrl(Order order, Transaction transaction) {
        if (vnpayTmnCode == null || vnpayTmnCode.trim().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình payment.vnpay.tmn-code");
        }

        if (vnpayHashSecret == null || vnpayHashSecret.trim().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình payment.vnpay.hash-secret");
        }

        String txnRef = order.getOrderId() + "-" + transaction.getTransactionId();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        String createDate = formatter.format(calendar.getTime());

        calendar.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(calendar.getTime());

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayTmnCode);

        // Đảm bảo số nhân với 100 là số nguyên
        params.put("vnp_Amount", String.valueOf((long) (transaction.getAmount() * 100)));

        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang SmartCart #" + order.getOrderId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", publicBaseUrl + "/api/v1/payments/vnpay/return");
        params.put("vnp_IpAddr", "127.0.0.1"); // Trong thực tế, nên lấy IP thực của client
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        String query = PaymentCryptoUtil.buildQuery(params);
        String secureHash = PaymentCryptoUtil.hmacSHA512(query, vnpayHashSecret);

        String paymentUrl = vnpayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;

        return PaymentCreateResult.builder()
                .paymentUrl(paymentUrl)
                .providerTransactionId(txnRef)
                .build();
    }
}