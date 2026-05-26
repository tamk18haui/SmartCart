package com.gr6.SmartCart.modules.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.gr6.SmartCart.common.domain.UserDeviceToken;
import com.gr6.SmartCart.common.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    public void sendToUser(Long userId, String title, String body, Map<String, String> data) {
        if (userId == null) {
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase Admin chưa được khởi tạo, bỏ qua push notification.");
            return;
        }

        for (UserDeviceToken deviceToken : userDeviceTokenRepository.findByUser_UserIdAndActiveTrue(userId)) {
            sendToToken(deviceToken, title, body, data);
        }
    }

    private void sendToToken(UserDeviceToken deviceToken, String title, String body, Map<String, String> data) {
        try {
            Message.Builder builder = Message.builder()
                    .setToken(deviceToken.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            FirebaseMessaging.getInstance().send(builder.build());
        } catch (Exception e) {
            log.warn("Gửi FCM lỗi, token sẽ bị tắt. tokenId={}, error={}",
                    deviceToken.getId(), e.getMessage());
            deviceToken.setActive(false);
            userDeviceTokenRepository.save(deviceToken);
        }
    }
}
