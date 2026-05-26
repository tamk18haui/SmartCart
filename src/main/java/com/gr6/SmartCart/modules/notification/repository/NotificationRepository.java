package com.gr6.SmartCart.modules.notification.repository;

import com.gr6.SmartCart.common.domain.Notification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Notification> findTop50ByUser_EmailOrderByCreatedAtDesc(String email);

    long countByUser_EmailAndIsReadFalse(String email);

    Optional<Notification> findByNotificationIdAndUser_Email(Long notificationId, String email);
}
