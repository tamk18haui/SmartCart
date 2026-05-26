package com.gr6.SmartCart.modules.finance_core.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PaymentCryptoUtil {

    private PaymentCryptoUtil() {
    }

    public static String hmacSHA512(String data, String key) {
        return hmac(data, key, "HmacSHA512");
    }

    public static String hmacSHA256(String data, String key) {
        return hmac(data, key, "HmacSHA256");
    }

    private static String hmac(String data, String key, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    algorithm
            );

            mac.init(secretKeySpec);

            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();

            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }

            return hash.toString();

        } catch (Exception e) {
            throw new RuntimeException("Không tạo được chữ ký thanh toán", e);
        }
    }

    public static String urlEncode(String value) {
        if (value == null) {
            return "";
        }

        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String buildQuery(Map<String, String> params) {
        return new TreeMap<>(params)
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != null && !e.getValue().trim().isEmpty())
                .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));
    }
}