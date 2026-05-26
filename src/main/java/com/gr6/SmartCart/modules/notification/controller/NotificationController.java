package com.gr6.SmartCart.modules.notification.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Notification;
import com.gr6.SmartCart.modules.notification.dto.NotificationResponse;
import com.gr6.SmartCart.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public BaseResponse<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        String email = authentication.getName();

        List<NotificationResponse> response = notificationRepository
                .findTop50ByUser_EmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return BaseResponse.success_data("Lấy danh sách thông báo thành công", response);
    }

    @GetMapping("/unread-count")
    public BaseResponse<Long> countUnread(Authentication authentication) {
        long count = notificationRepository.countByUser_EmailAndIsReadFalse(authentication.getName());
        return BaseResponse.success_data("Lấy số thông báo chưa đọc thành công", count);
    }

    @PutMapping("/{notificationId}/read")
    public BaseResponse<NotificationResponse> markAsRead(
            Authentication authentication,
            @PathVariable Long notificationId
    ) {
        Notification notification = notificationRepository
                .findByNotificationIdAndUser_Email(notificationId, authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        notification.setIsRead(true);
        notificationRepository.save(notification);

        return BaseResponse.success_data("Đã đọc thông báo", mapToResponse(notification));
    }

    @PutMapping("/read-all")
    public BaseResponse<?> markAllAsRead(Authentication authentication) {
        List<Notification> notifications = notificationRepository
                .findTop50ByUser_EmailOrderByCreatedAtDesc(authentication.getName());

        for (Notification notification : notifications) {
            if (!Boolean.TRUE.equals(notification.getIsRead())) {
                notification.setIsRead(true);
            }
        }

        notificationRepository.saveAll(notifications);
        return BaseResponse.successMessage("Đã đánh dấu tất cả thông báo là đã đọc");
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .isRead(Boolean.TRUE.equals(notification.getIsRead()))
                .routeKey(notification.getRouteKey())
                .targetId(notification.getTargetId())
                .routeParams(notification.getRouteParams())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
