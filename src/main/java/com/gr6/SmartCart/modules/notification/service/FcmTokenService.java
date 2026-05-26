package com.gr6.SmartCart.modules.notification.service;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.domain.UserDeviceToken;
import com.gr6.SmartCart.common.repository.UserDeviceTokenRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.notification.dto.FcmTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final UserRepository userRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;

    @Transactional
    public void saveToken(String email, FcmTokenRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"));

        UserDeviceToken token = userDeviceTokenRepository.findByFcmToken(request.getFcmToken())
                .orElseGet(UserDeviceToken::new);

        token.setUser(user);
        token.setFcmToken(request.getFcmToken());
        token.setDeviceId(request.getDeviceId());
        token.setPlatform(request.getPlatform() == null || request.getPlatform().isBlank()
                ? "ANDROID"
                : request.getPlatform().trim().toUpperCase());
        token.setActive(true);

        userDeviceTokenRepository.save(token);
    }

    @Transactional
    public void disableToken(String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) {
            return;
        }

        userDeviceTokenRepository.findByFcmToken(fcmToken)
                .ifPresent(token -> {
                    token.setActive(false);
                    userDeviceTokenRepository.save(token);
                });
    }
}
