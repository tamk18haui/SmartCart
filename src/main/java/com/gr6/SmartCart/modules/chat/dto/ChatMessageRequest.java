package com.gr6.SmartCart.modules.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotNull(message = "receiverId không được trống")
    private Long receiverId;

    @NotBlank(message = "content không được trống")
    private String content;
}
