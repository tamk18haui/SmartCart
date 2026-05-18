package com.gr6.SmartCart.modules.chat.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.chat.dto.ChatMessageResponse;
import com.gr6.SmartCart.modules.chat.dto.ConversationResponse;
import com.gr6.SmartCart.modules.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public BaseResponse<List<ConversationResponse>> getMyConversations(Authentication authentication) {
        return BaseResponse.success(chatService.getMyConversations(authentication));
    }

    @GetMapping("/messages/{partnerId}")
    public BaseResponse<Page<ChatMessageResponse>> getMessages(
            Authentication authentication,
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return BaseResponse.success(chatService.getMessagesWithUser(authentication, partnerId, page, size));
    }

    @PatchMapping("/messages/{partnerId}/read")
    public BaseResponse<Integer> markAsRead(Authentication authentication, @PathVariable Long partnerId) {
        return BaseResponse.success_data("Đã đánh dấu đã đọc", chatService.markAsRead(authentication, partnerId));
    }
}
