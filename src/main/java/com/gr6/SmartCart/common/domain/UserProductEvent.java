package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.RecommendationEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "User_Product_Events",
        indexes = {
                @Index(name = "idx_reco_event_user_time", columnList = "user_id, created_at"),
                @Index(name = "idx_reco_event_product", columnList = "product_id"),
                @Index(name = "idx_reco_event_type", columnList = "event_type")
        }
)
@Getter
@Setter
public class UserProductEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private RecommendationEventType eventType;

    @Column(columnDefinition = "TEXT")
    private String keyword;

    @Column(nullable = false)
    private Integer quantity = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}