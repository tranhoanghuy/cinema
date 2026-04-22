package com.cinetix.chat.application;

import com.cinetix.chat.domain.model.*;
import com.cinetix.chat.infrastructure.persistence.*;
import com.cinetix.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatApplicationService {

    private final ConversationJpaRepository conversationRepo;
    private final ChatMessageJpaRepository  messageRepo;

    public Conversation createConversation(UUID customerId, String subject) {
        Conversation conv = Conversation.builder()
            .customerId(customerId)
            .subject(subject)
            .build();
        return conversationRepo.save(conv);
    }

    public ChatMessage sendMessage(UUID conversationId, UUID senderId,
                                    SenderType senderType, String content) {
        Conversation conv = conversationRepo.findById(conversationId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        if (conv.getStatus() == ConversationStatus.CLOSED)
            throw new IllegalStateException("Conversation is closed");

        ChatMessage msg = ChatMessage.builder()
            .conversation(conv)
            .senderId(senderId)
            .senderType(senderType)
            .content(content)
            .build();

        conv.setUpdatedAt(java.time.Instant.now());
        conversationRepo.save(conv);
        return messageRepo.save(msg);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getCustomerConversations(UUID customerId) {
        return conversationRepo.findByCustomerIdOrderByUpdatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(UUID conversationId, int page, int size) {
        return messageRepo.findByConversationId(conversationId, PageRequest.of(page, size)).getContent();
    }

    public void closeConversation(UUID conversationId) {
        Conversation conv = conversationRepo.findById(conversationId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
        conv.close();
        conversationRepo.save(conv);
    }
}
