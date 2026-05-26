package com.gr6.SmartCart.modules.notification.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseAdminConfig {

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @PostConstruct
    public void initFirebase() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase Admin đã được khởi tạo trước đó.");
                return;
            }

            if (serviceAccountPath == null || serviceAccountPath.trim().isEmpty()) {
                log.warn("Chưa cấu hình firebase.service-account-path, tạm bỏ qua Firebase Admin.");
                return;
            }

            File serviceAccountFile = new File(serviceAccountPath);

            if (!serviceAccountFile.exists() || !serviceAccountFile.isFile()) {
                log.error("Không tìm thấy file Firebase service account tại: {}", serviceAccountPath);
                return;
            }

            try (InputStream inputStream = new FileInputStream(serviceAccountFile)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin đã khởi tạo thành công. serviceAccountPath={}", serviceAccountPath);
            }
        } catch (Exception e) {
            log.error("Không khởi tạo được Firebase Admin: {}", e.getMessage(), e);
        }
    }
}