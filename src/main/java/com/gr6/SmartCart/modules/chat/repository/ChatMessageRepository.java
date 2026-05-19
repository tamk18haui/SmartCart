package com.gr6.SmartCart.modules.chat.repository;

import com.gr6.SmartCart.common.domain.Conversation;
import com.gr6.SmartCart.common.domain.Message;
import com.gr6.SmartCart.common.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ChatMessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);

    long countByConversationAndReceiverAndReadAtIsNull(Conversation conversation, User receiver);

    @Modifying
    @Query("""
            UPDATE Message m
            SET m.readAt = :readAt
            WHERE m.conversation = :conversation
              AND m.receiver = :receiver
              AND m.readAt IS NULL
            """)
    int markConversationAsRead(
            @Param("conversation") Conversation conversation,
            @Param("receiver") User receiver,
            @Param("readAt") LocalDateTime readAt
    );
}
