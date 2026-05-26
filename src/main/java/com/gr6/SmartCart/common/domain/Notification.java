package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "Notifications",
        indexes = {
                @Index(name = "idx_notification_user_read", columnList = "user_id,is_read"),
                @Index(name = "idx_notification_user_created", columnList = "user_id,created_at")
        }
)
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private NotificationType type;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "route_key", length = 80)
    private String routeKey;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "route_params", columnDefinition = "TEXT")
    private String routeParams;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
