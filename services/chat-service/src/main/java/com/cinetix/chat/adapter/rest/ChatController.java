package com.cinetix.chat.adapter.rest;

import com.cinetix.chat.application.ChatApplicationService;
import com.cinetix.chat.domain.model.ChatMessage;
import com.cinetix.chat.domain.model.Conversation;
import com.cinetix.common.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ChatController {

    private final ChatApplicationService appService;

    @PostMapping
    public ResponseEntity<ApiResponse<Conversation>> create(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody CreateConversationRequest req) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(201).body(
            ApiResponse.success(appService.createConversation(customerId, req.subject())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Conversation>>> list(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(appService.getCustomerConversations(customerId)));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getMessages(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(appService.getMessages(id, page, size)));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<String>> close(@PathVariable UUID id) {
        appService.closeConversation(id);
        return ResponseEntity.ok(ApiResponse.success("Conversation closed"));
    }

    public record CreateConversationRequest(String subject) {}
}
