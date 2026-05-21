package com.gr6.SmartCart.modules.chat.controller;

import com.gr6.SmartCart.common.security.JwtTokenProvider;
import com.gr6.SmartCart.modules.chat.dto.ChatMessageRequest;
import com.gr6.SmartCart.modules.chat.dto.ChatMessageResponse;
import com.gr6.SmartCart.modules.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @MessageMapping("/chat.send")
    public void sendMessage(
            @Valid @Payload ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String token = extractToken(headerAccessor);

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Bạn cần đăng nhập để chat");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }

        String senderEmail = jwtTokenProvider.getEmailFromJwt(token);

        ChatMessageResponse response = chatService.saveMessage(senderEmail, request);

        // Gửi về người nhận
        messagingTemplate.convertAndSend(
                "/topic/chat/" + response.getReceiverId(),
                response
        );

        // Gửi lại cho chính người gửi để UI cập nhật tin nhắn vừa gửi
        messagingTemplate.convertAndSend(
                "/topic/chat/" + response.getSenderId(),
                response
        );
    }

    private String extractToken(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor == null) {
            return null;
        }

        String authorization = headerAccessor.getFirstNativeHeader("Authorization");

        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        if (!authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring(7).trim();
    }
}