package com.gr6.SmartCart.modules.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationResponse {
    private Long conversationId;
    private Long partnerId;
    private String partnerName;
    private String partnerAvatarUrl;
    private String lastMessage;
    private LocalDateTime updatedAt;
    private long unreadCount;
}
