package com.gr6.SmartCart.modules.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotNull(message = "receiverId không được trống")
    private Long receiverId;

    // Có thể rỗng nếu gửi ảnh.
    private String content;

    // Link ảnh sau khi Android upload lên Cloudinary/backend.
    private String imageUrl;

    // TEXT / IMAGE. Nếu bỏ trống backend tự suy ra.
    private String messageType;
}
