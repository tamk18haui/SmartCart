package com.gr6.SmartCart.module_v3.recommendation.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gr6.SmartCart.module_v3.recommendation.ai.dto.AiImageSearchRequest;
import com.gr6.SmartCart.module_v3.recommendation.ai.dto.AiIndexRequest;
import com.gr6.SmartCart.module_v3.recommendation.ai.dto.AiProductCandidate;
import com.gr6.SmartCart.module_v3.recommendation.ai.dto.AiRecommendResponse;
import com.gr6.SmartCart.module_v3.recommendation.ai.dto.AiTextQueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AiServerClient {

    private final ObjectMapper objectMapper;

    @Value("${ai.recommendation.base-url:http://127.0.0.1:8001}")
    private String aiBaseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    public boolean indexProducts(List<AiProductCandidate> candidates) {
        try {
            String url = aiBaseUrl + "/index/products";

            AiIndexRequest request = new AiIndexRequest(
                    candidates == null ? List.of() : candidates
            );

            String json = objectMapper.writeValueAsString(request);

            System.out.println("===== SEND TO AI /index/products =====");
            System.out.println("products size = " + (candidates == null ? 0 : candidates.size()));
            System.out.println("POST AI body bytes = " + json.getBytes(StandardCharsets.UTF_8).length);

            String body = postJsonRaw(url, json, 120);

            return body != null && !body.isBlank();

        } catch (Exception e) {
            System.out.println("ERROR AI indexProducts: " + e.getMessage());
            return false;
        }
    }

    public AiRecommendResponse recommendByText(AiTextQueryRequest request) {
        try {
            String url = aiBaseUrl + "/recommend/text";
            String json = objectMapper.writeValueAsString(request);

            String body = postJsonRaw(url, json, 8);

            if (body == null || body.isBlank()) {
                return new AiRecommendResponse();
            }

            return objectMapper.readValue(body, AiRecommendResponse.class);

        } catch (Exception e) {
            System.out.println("ERROR AI recommendByText: " + e.getMessage());
            return new AiRecommendResponse();
        }
    }

    public AiRecommendResponse searchByImage(
            MultipartFile file,
            int page,
            int size
    ) {
        try {
            String url = aiBaseUrl + "/search/image-base64";

            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                originalName = "search-image.jpg";
            }

            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "image/jpeg";
            }

            byte[] optimizedImageBytes = optimizeImage(file.getBytes(), 512);
            String imageBase64 = Base64.getEncoder().encodeToString(optimizedImageBytes);

            AiImageSearchRequest request = new AiImageSearchRequest(
                    originalName,
                    contentType,
                    imageBase64,
                    List.of(),
                    page,
                    size
            );

            String json = objectMapper.writeValueAsString(request);

            String body = postJsonRaw(url, json, 20);

            if (body == null || body.isBlank()) {
                return new AiRecommendResponse();
            }

            return objectMapper.readValue(body, AiRecommendResponse.class);

        } catch (Exception e) {
            System.out.println("ERROR AI searchByImage: " + e.getMessage());
            return new AiRecommendResponse();
        }
    }

    private String postJsonRaw(String url, String json, int timeoutSeconds) {
        try {
            byte[] bodyBytes = json.getBytes(StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println("AI ERROR status = " + response.statusCode());
                System.out.println("AI ERROR body = " + preview(response.body()));
                return "";
            }

            return response.body();

        } catch (Exception e) {
            System.out.println("ERROR postJsonRaw to AI: " + e.getMessage());
            return "";
        }
    }

    private byte[] optimizeImage(byte[] originalBytes, int maxSize) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));

            if (originalImage == null) {
                return originalBytes;
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            int targetWidth = originalWidth;
            int targetHeight = originalHeight;

            if (originalWidth > maxSize || originalHeight > maxSize) {
                double scale = Math.min(
                        (double) maxSize / originalWidth,
                        (double) maxSize / originalHeight
                );

                targetWidth = Math.max(1, (int) Math.round(originalWidth * scale));
                targetHeight = Math.max(1, (int) Math.round(originalHeight * scale));
            }

            Image scaledImage = originalImage.getScaledInstance(
                    targetWidth,
                    targetHeight,
                    Image.SCALE_SMOOTH
            );

            BufferedImage outputImage = new BufferedImage(
                    targetWidth,
                    targetHeight,
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D graphics = outputImage.createGraphics();
            graphics.drawImage(scaledImage, 0, 0, null);
            graphics.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(outputImage, "jpg", outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            return originalBytes;
        }
    }

    private String preview(String value) {
        if (value == null) return "";
        int max = Math.min(300, value.length());
        return value.substring(0, max);
    }
}