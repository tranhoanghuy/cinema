package com.cinetix.chat.adapter.websocket;

import com.cinetix.chat.application.ChatApplicationService;
import com.cinetix.chat.domain.model.ChatMessage;
import com.cinetix.chat.domain.model.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * STOMP WebSocket controller for real-time chat.
 * Clients subscribe to: /user/queue/chat/{conversationId}
 * Clients send to:      /app/chat/{conversationId}/send
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatApplicationService  appService;
    private final SimpMessagingTemplate   messagingTemplate;

    @MessageMapping("/chat/{conversationId}/send")
    public void sendMessage(@DestinationVariable UUID conversationId,
                             @Payload ChatMessagePayload payload,
                             SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal user = headerAccessor.getUser();
            UUID senderId = user != null ? UUID.fromString(user.getName()) : UUID.randomUUID();

            ChatMessage saved = appService.sendMessage(
                conversationId, senderId, SenderType.CUSTOMER, payload.content()
            );

            // Broadcast to all subscribers of this conversation
            messagingTemplate.convertAndSend(
                "/topic/chat/" + conversationId,
                new ChatMessageResponse(saved.getId(), saved.getSenderId(),
                    saved.getSenderType().name(), saved.getContent(), saved.getSentAt().toString())
            );
        } catch (Exception e) {
            log.error("Failed to process chat message: {}", e.getMessage(), e);
        }
    }

    public record ChatMessagePayload(String content) {}

    public record ChatMessageResponse(UUID id, UUID senderId, String senderType,
                                       String content, String sentAt) {}
}
