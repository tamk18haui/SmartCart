package com.gr6.SmartCart.modules.notification.service;

import com.gr6.SmartCart.common.domain.Notification;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.NotificationType;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppNotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FcmPushService fcmPushService;

    @Transactional
    public void notifyUser(
            Long userId,
            String title,
            String content,
            NotificationType type,
            Map<String, String> data
    ) {
        notifyUser(userId, title, content, type, null, null, null, data);
    }

    @Transactional
    public void notifyUser(
            Long userId,
            String title,
            String content,
            NotificationType type,
            String routeKey,
            Long targetId,
            String routeParams,
            Map<String, String> data
    ) {
        if (userId == null) {
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type == null ? NotificationType.SYSTEM : type);
        notification.setIsRead(false);
        notification.setRouteKey(routeKey);
        notification.setTargetId(targetId);
        notification.setRouteParams(routeParams);
        notificationRepository.save(notification);

        if (data != null) {
            if (routeKey != null) data.put("routeKey", routeKey);
            if (targetId != null) data.put("targetId", String.valueOf(targetId));
        }

        fcmPushService.sendToUser(userId, title, content, data);
    }
}
