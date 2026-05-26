package com.gr6.SmartCart.modules.chat.service;

import com.gr6.SmartCart.common.domain.Conversation;
import com.gr6.SmartCart.common.domain.Message;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.NotificationType;
import com.gr6.SmartCart.modules.chat.dto.ChatMessageRequest;
import com.gr6.SmartCart.modules.chat.dto.ChatMessageResponse;
import com.gr6.SmartCart.modules.chat.dto.ConversationResponse;
import com.gr6.SmartCart.modules.chat.repository.ChatMessageRepository;
import com.gr6.SmartCart.modules.chat.repository.ConversationRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.notification.service.AppNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final AppNotificationService appNotificationService;

    @Transactional
    public ChatMessageResponse saveMessage(String senderEmail, ChatMessageRequest request) {
        User sender = getUserByEmail(senderEmail);
        return saveMessage(sender, request);
    }

    @Transactional
    public ChatMessageResponse saveMessageBySenderId(Long senderId, ChatMessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"));
        return saveMessage(sender, request);
    }

    private ChatMessageResponse saveMessage(User sender, ChatMessageRequest request) {
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận"));

        if (sender.getUserId().equals(receiver.getUserId())) {
            throw new RuntimeException("Không thể tự chat với chính mình");
        }

        Conversation conversation = getOrCreateConversation(sender, receiver);

        String content = request.getContent() == null ? "" : request.getContent().trim();
        String imageUrl = request.getImageUrl() == null ? "" : request.getImageUrl().trim();

        if (content.isBlank() && imageUrl.isBlank()) {
            throw new RuntimeException("Tin nhắn không được trống");
        }

        String messageType = request.getMessageType();
        if (messageType == null || messageType.isBlank()) {
            messageType = imageUrl.isBlank() ? "TEXT" : "IMAGE";
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content.isBlank() ? "[Ảnh]" : content);
        message.setImageUrl(imageUrl.isBlank() ? null : imageUrl);
        message.setMessageType(messageType.trim().toUpperCase());

        Message saved = messageRepository.save(message);
        conversation.setLastMessage("IMAGE".equalsIgnoreCase(saved.getMessageType()) && content.isBlank()
                ? "[Ảnh]"
                : saved.getContent());
        conversationRepository.save(conversation);

        appNotificationService.notifyUser(
                receiver.getUserId(),
                "Tin nhắn mới từ " + safeName(sender),
                saved.getContent(),
                NotificationType.CHAT,
                Map.of(
                        "type", "CHAT",
                        "senderId", String.valueOf(sender.getUserId()),
                        "conversationId", String.valueOf(conversation.getConversationId())
                )
        );

        return toMessageResponse(saved);
    }

    @Transactional
    public List<ConversationResponse> getMyConversations(Authentication authentication) {
        User currentUser = getUserByEmail(authentication.getName());
        return conversationRepository.findAllByUser(currentUser)
                .stream()
                .map(conversation -> toConversationResponse(conversation, currentUser))
                .sorted(Comparator.comparing(ConversationResponse::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public Page<ChatMessageResponse> getMessagesWithUser(Authentication authentication, Long partnerId, int page, int size) {
        User currentUser = getUserByEmail(authentication.getName());
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Conversation conversation = conversationRepository.findByUsers(currentUser, partner)
                .orElseThrow(() -> new RuntimeException("Chưa có cuộc trò chuyện"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return messageRepository.findMessagesWithUsers(conversation, pageable)
                .map(this::toMessageResponse);
    }

    @Transactional
    public int markAsRead(Authentication authentication, Long partnerId) {
        User currentUser = getUserByEmail(authentication.getName());
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Conversation conversation = conversationRepository.findByUsers(currentUser, partner)
                .orElseThrow(() -> new RuntimeException("Chưa có cuộc trò chuyện"));

        return messageRepository.markConversationAsRead(conversation, currentUser, LocalDateTime.now());
    }

    private Conversation getOrCreateConversation(User userA, User userB) {
        return conversationRepository.findByUsers(userA, userB)
                .orElseGet(() -> {
                    User userOne = userA.getUserId() < userB.getUserId() ? userA : userB;
                    User userTwo = userA.getUserId() < userB.getUserId() ? userB : userA;
                    Conversation conversation = Conversation.builder()
                            .userOne(userOne)
                            .userTwo(userTwo)
                            .lastMessage("")
                            .build();
                    return conversationRepository.save(conversation);
                });
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"));
    }

    private String safeName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        return user.getEmail();
    }

    private ConversationResponse toConversationResponse(Conversation conversation, User currentUser) {
        User partner = conversation.getUserOne().getUserId().equals(currentUser.getUserId())
                ? conversation.getUserTwo()
                : conversation.getUserOne();

        return ConversationResponse.builder()
                .conversationId(conversation.getConversationId())
                .partnerId(partner.getUserId())
                .partnerName(partner.getFullName())
                .partnerAvatarUrl(partner.getAvatarUrl())
                .lastMessage(conversation.getLastMessage())
                .updatedAt(conversation.getUpdatedAt())
                .unreadCount(messageRepository.countByConversationAndReceiverAndReadAtIsNull(conversation, currentUser))
                .build();
    }

    private ChatMessageResponse toMessageResponse(Message message) {
        return ChatMessageResponse.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversation() == null ? null : message.getConversation().getConversationId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getFullName())
                .senderAvatarUrl(message.getSender().getAvatarUrl())
                .receiverId(message.getReceiver().getUserId())
                .receiverName(message.getReceiver().getFullName())
                .receiverAvatarUrl(message.getReceiver().getAvatarUrl())
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .messageType(message.getMessageType())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .build();
    }
}
