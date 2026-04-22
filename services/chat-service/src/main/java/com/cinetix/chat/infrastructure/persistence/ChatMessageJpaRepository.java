package com.cinetix.chat.infrastructure.persistence;

import com.cinetix.chat.domain.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, UUID> {
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :convId ORDER BY m.sentAt ASC")
    Page<ChatMessage> findByConversationId(@Param("convId") UUID conversationId, Pageable pageable);
}
