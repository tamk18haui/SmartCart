package com.gr6.SmartCart.modules.finance_core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gr6.SmartCart.common.domain.Order;
import com.gr6.SmartCart.common.domain.Transaction;
import com.gr6.SmartCart.common.enums.PaymentProvider;
import com.gr6.SmartCart.modules.finance_core.dto.PaymentCreateResult;
import com.gr6.SmartCart.modules.finance_core.service.PaymentGatewayService;
import com.gr6.SmartCart.modules.finance_core.util.PaymentCryptoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DevPaymentGatewayServiceImpl implements PaymentGatewayService {

    public static final String DEV_PAYMENT_SIGNATURE = "SMARTCART_DEV_PAYMENT_SECRET";

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Value("${payment.vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpayUrl;

    @Value("${payment.vnpay.tmn-code:}")
    private String vnpayTmnCode;

    @Value("${payment.vnpay.hash-secret:}")
    private String vnpayHashSecret;

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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public PaymentCreateResult createPaymentUrl(
            Order order,
            Transaction transaction,
            PaymentProvider provider
    ) {
        if (provider == null || provider == PaymentProvider.NONE) {
            throw new RuntimeException("Vui lòng chọn cổng thanh toán");
        }

        if (provider == PaymentProvider.MOMO) {
            return createMomoPaymentUrl(order, transaction);
        }

        if (provider == PaymentProvider.VNPAY) {
            return createVnpayPaymentUrl(order, transaction);
        }

        throw new RuntimeException("Cổng thanh toán không hợp lệ");
    }

    private PaymentCreateResult createMomoPaymentUrl(
            Order order,
            Transaction transaction
    ) {
        if (order == null || order.getOrderId() == null) {
            throw new RuntimeException("Đơn hàng không hợp lệ");
        }

        if (transaction == null || transaction.getTransactionId() == null) {
            throw new RuntimeException("Giao dịch thanh toán không hợp lệ");
        }

        Long amount = transaction.getAmount();

        if (amount == null || amount <= 0) {
            throw new RuntimeException("Số tiền thanh toán không hợp lệ");
        }

        if (amount < 1000) {
            throw new RuntimeException("MoMo test yêu cầu số tiền tối thiểu 1.000đ");
        }

        if (amount > 50_000_000L) {
            throw new RuntimeException("MoMo test chỉ hỗ trợ số tiền tối đa 50.000.000đ");
        }

        validateMomoConfig();

        String baseUrl = normalizeBaseUrl(publicBaseUrl);

        String momoOrderId = "SC" + order.getOrderId()
                + "TX" + transaction.getTransactionId();

        String requestId = "REQ" + order.getOrderId()
                + "TX" + transaction.getTransactionId()
                + "T" + System.currentTimeMillis();

        String orderInfo = "Thanh toan don hang SmartCart #" + order.getOrderId();

        /*
         * redirectUrl/ipnUrl phải là HTTPS public URL.
         * Không dùng localhost, không dùng smartcart:// trực tiếp ở đây.
         * MoMo sẽ quay về backend trước, backend mới redirect về app.
         */
        String redirectUrl = baseUrl + "/api/v1/payments/momo/return";
        String ipnUrl = baseUrl + "/api/v1/payments/momo/ipn";

        /*
         * MoMo docs cho phép extraData rỗng,
         * nhưng để ổn định hơn thì dùng base64 của JSON rỗng: {}
         */
        String extraData = "";

        String rawSignature =
                "accessKey=" + momoAccessKey.trim()
                        + "&amount=" + amount
                        + "&extraData=" + extraData
                        + "&ipnUrl=" + ipnUrl
                        + "&orderId=" + momoOrderId
                        + "&orderInfo=" + orderInfo
                        + "&partnerCode=" + momoPartnerCode.trim()
                        + "&redirectUrl=" + redirectUrl
                        + "&requestId=" + requestId
                        + "&requestType=" + momoRequestType.trim();

        String signature = PaymentCryptoUtil.hmacSHA256(
                rawSignature,
                momoSecretKey.trim()
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", momoPartnerCode.trim());
        body.put("partnerName", "SmartCart");
        body.put("storeId", "SmartCartStore");
        body.put("requestId", requestId);
        body.put("amount", amount);
        body.put("orderId", momoOrderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("lang", "vi");
        body.put("requestType", momoRequestType.trim());
        body.put("autoCapture", true);
        body.put("extraData", extraData);
        body.put("signature", signature);

        System.out.println("========== MOMO CREATE REQUEST ==========");
        System.out.println("endpoint = " + momoEndpoint);
        System.out.println("partnerCode = " + momoPartnerCode);
        System.out.println("orderId = " + order.getOrderId());
        System.out.println("transactionId = " + transaction.getTransactionId());
        System.out.println("momoOrderId = " + momoOrderId);
        System.out.println("requestId = " + requestId);
        System.out.println("amount = " + amount);
        System.out.println("redirectUrl = " + redirectUrl);
        System.out.println("ipnUrl = " + ipnUrl);
        System.out.println("rawSignature = " + rawSignature);
        System.out.println("signature = " + signature);
        System.out.println("body = " + body);
        System.out.println("=========================================");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    momoEndpoint.trim(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String rawResponse = response.getBody();

            System.out.println("========== MOMO CREATE RESPONSE ==========");
            System.out.println("httpStatus = " + response.getStatusCode());
            System.out.println("rawResponse = " + rawResponse);
            System.out.println("==========================================");

            JsonNode json = objectMapper.readTree(rawResponse);

            int resultCode = json.path("resultCode").asInt(-1);
            String message = json.path("message").asText("");

            String payUrl = json.path("payUrl").asText("");
            String deeplink = json.path("deeplink").asText("");
            String deeplinkMiniApp = json.path("deeplinkMiniApp").asText("");
            String qrCodeUrl = json.path("qrCodeUrl").asText("");

            System.out.println("========== MOMO CREATE RESPONSE ==========");
            System.out.println("httpStatus = " + response.getStatusCode());
            System.out.println("resultCode = " + resultCode);
            System.out.println("message = " + message);
            System.out.println("payUrl = " + payUrl);
            System.out.println("deeplink = " + deeplink);
            System.out.println("deeplinkMiniApp = " + deeplinkMiniApp);
            System.out.println("qrCodeUrl = " + qrCodeUrl);
            System.out.println("rawResponse = " + rawResponse);
            System.out.println("==========================================");

            if (resultCode != 0) {
                throw new RuntimeException(
                        "MoMo tạo thanh toán thất bại. resultCode="
                                + resultCode
                                + ", message="
                                + message
                                + ", rawResponse="
                                + rawResponse
                );
            }

            /*
             * Android nên ưu tiên deeplink để mở MoMo Test App.
             * Nếu dùng payUrl thì dễ rơi vào payType=webApp như log của bạn.
             */
            String openUrl = "";

            if (deeplink != null && !deeplink.trim().isEmpty()) {
                openUrl = deeplink.trim();
            } else if (deeplinkMiniApp != null && !deeplinkMiniApp.trim().isEmpty()) {
                openUrl = deeplinkMiniApp.trim();
            } else if (qrCodeUrl != null && !qrCodeUrl.trim().isEmpty()) {
                openUrl = qrCodeUrl.trim();
            } else if (payUrl != null && !payUrl.trim().isEmpty()) {
                openUrl = payUrl.trim();
            }

            if (openUrl.isEmpty()) {
                throw new RuntimeException("MoMo không trả URL thanh toán. rawResponse=" + rawResponse);
            }

            return PaymentCreateResult.builder()
                    .paymentUrl(openUrl)
                    .providerTransactionId(momoOrderId)
                    .build();

        } catch (HttpStatusCodeException e) {
            /*
             * Đây là chỗ quan trọng để thấy MoMo 400 thật sự báo gì.
             */
            System.out.println("========== MOMO HTTP ERROR ==========");
            System.out.println("status = " + e.getStatusCode());
            System.out.println("responseBody = " + e.getResponseBodyAsString());
            System.out.println("=====================================");

            throw new RuntimeException(
                    "Không thể tạo URL MoMo. HTTP "
                            + e.getStatusCode().value()
                            + ": "
                            + e.getResponseBodyAsString(),
                    e
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Không tạo được URL thanh toán MoMo: " + e.getMessage(),
                    e
            );
        }
    }

    private void validateMomoConfig() {
        if (momoEndpoint == null || momoEndpoint.trim().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình payment.momo.endpoint");
        }

        if (momoPartnerCode == null || momoPartnerCode.trim().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình payment.momo.partner-code");
        }

        if (momoAccessKey == null || momoAccessKey.trim().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình payment.momo.access-key");
        }

        if (momoSecretKey == null || momoSecretKey.trim().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình payment.momo.secret-key");
        }

        if (momoRequestType == null || momoRequestType.trim().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình payment.momo.request-type");
        }
    }

    private PaymentCreateResult createVnpayPaymentUrl(
            Order order,
            Transaction transaction
    ) {
        if (order == null || order.getOrderId() == null) {
            throw new RuntimeException("Đơn hàng không hợp lệ");
        }

        if (transaction == null || transaction.getTransactionId() == null) {
            throw new RuntimeException("Giao dịch thanh toán không hợp lệ");
        }

        if (vnpayTmnCode == null || vnpayTmnCode.trim().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình payment.vnpay.tmn-code");
        }

        if (vnpayHashSecret == null || vnpayHashSecret.trim().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình payment.vnpay.hash-secret");
        }

        Long amount = transaction.getAmount();

        if (amount == null || amount <= 0) {
            throw new RuntimeException("Số tiền thanh toán không hợp lệ");
        }

        String baseUrl = normalizeBaseUrl(publicBaseUrl);
        String txnRef = order.getOrderId() + "-" + transaction.getTransactionId();

        TimeZone vnTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar calendar = Calendar.getInstance(vnTimeZone);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(vnTimeZone);

        String createDate = formatter.format(calendar.getTime());

        calendar.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(calendar.getTime());

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayTmnCode.trim());
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang SmartCart " + order.getOrderId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", baseUrl + "/api/v1/payments/vnpay/return");
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        String query = PaymentCryptoUtil.buildQuery(params);

        String secureHash = PaymentCryptoUtil.hmacSHA512(
                query,
                vnpayHashSecret.trim()
        );

        String paymentUrl = vnpayUrl.trim()
                + "?"
                + query
                + "&vnp_SecureHash="
                + secureHash;

        return PaymentCreateResult.builder()
                .paymentUrl(paymentUrl)
                .providerTransactionId(txnRef)
                .build();
    }

    private String normalizeBaseUrl(String rawBaseUrl) {
        String baseUrl = rawBaseUrl;

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "http://localhost:8080";
        }

        baseUrl = baseUrl.trim();

        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl;
    }
}