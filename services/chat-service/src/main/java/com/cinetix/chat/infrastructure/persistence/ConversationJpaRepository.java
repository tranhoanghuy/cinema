package com.cinetix.chat.infrastructure.persistence;

import com.cinetix.chat.domain.model.Conversation;
import com.cinetix.chat.domain.model.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationJpaRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByCustomerIdOrderByUpdatedAtDesc(UUID customerId);
    List<Conversation> findByStatusOrderByCreatedAtAsc(ConversationStatus status);
}
