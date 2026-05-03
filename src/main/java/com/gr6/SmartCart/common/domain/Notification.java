package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "Notifications")
@Data
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private NotificationType type;

    private Boolean isRead;

    @CreationTimestamp
    private LocalDateTime createdAt;
}