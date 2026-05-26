package com.gr6.SmartCart.modules.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatarUrl;
    private Long receiverId;
    private String receiverName;
    private String receiverAvatarUrl;
    private String content;
    private String imageUrl;
    private String messageType;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
