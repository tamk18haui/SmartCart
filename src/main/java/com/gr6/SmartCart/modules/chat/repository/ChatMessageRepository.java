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

import java.time.LocalDateTime;

public interface ChatMessageRepository extends JpaRepository<Message, Long> {

    @Query(
            value = """
                    SELECT m FROM Message m
                    JOIN FETCH m.sender
                    JOIN FETCH m.receiver
                    JOIN FETCH m.conversation
                    WHERE m.conversation = :conversation
                    ORDER BY m.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(m) FROM Message m
                    WHERE m.conversation = :conversation
                    """
    )
    Page<Message> findMessagesWithUsers(
            @Param("conversation") Conversation conversation,
            Pageable pageable
    );

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