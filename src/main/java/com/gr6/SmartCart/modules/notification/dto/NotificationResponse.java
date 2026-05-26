package com.gr6.SmartCart.modules.notification.dto;

import com.gr6.SmartCart.common.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long notificationId;
    private String title;
    private String content;
    private NotificationType type;
    private Boolean isRead;
    private String routeKey;
    private Long targetId;
    private String routeParams;
    private LocalDateTime createdAt;
}
