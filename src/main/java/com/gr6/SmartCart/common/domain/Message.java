package com.gr6.SmartCart.common.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "Messages")
@Getter
@Setter
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;
}