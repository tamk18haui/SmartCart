package com.gr6.SmartCart.modules.chat.controller;

import com.gr6.SmartCart.modules.chat.dto.ChatMessageRequest;
import com.gr6.SmartCart.modules.chat.dto.ChatMessageResponse;
import com.gr6.SmartCart.modules.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid @Payload ChatMessageRequest request, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new RuntimeException("Bạn cần đăng nhập để chat");
        }

        Long senderId = Long.valueOf(principal.getName());
        ChatMessageResponse response = chatService.saveMessageBySenderId(senderId, request);

        messagingTemplate.convertAndSendToUser(
                response.getReceiverId().toString(),
                "/queue/messages",
                response
        );

        messagingTemplate.convertAndSendToUser(
                response.getSenderId().toString(),
                "/queue/messages",
                response
        );
    }
}
