package com.gr6.SmartCart.modules.chat.repository;

import com.gr6.SmartCart.common.domain.Conversation;
import com.gr6.SmartCart.common.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
            SELECT c FROM Conversation c
            JOIN FETCH c.userOne
            JOIN FETCH c.userTwo
            WHERE (c.userOne = :userA AND c.userTwo = :userB)
               OR (c.userOne = :userB AND c.userTwo = :userA)
            """)
    Optional<Conversation> findByUsers(
            @Param("userA") User userA,
            @Param("userB") User userB
    );

    @Query("""
            SELECT c FROM Conversation c
            JOIN FETCH c.userOne
            JOIN FETCH c.userTwo
            WHERE c.userOne = :user OR c.userTwo = :user
            ORDER BY c.updatedAt DESC
            """)
    List<Conversation> findAllByUser(@Param("user") User user);
}