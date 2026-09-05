package com.kyawhsan.notiva.ai.controller;

import com.kyawhsan.notiva.ai.dto.AiMessageResponse;
import com.kyawhsan.notiva.common.response.ApiResponse;
import com.kyawhsan.notiva.ai.dto.ConversationResponse;
import com.kyawhsan.notiva.ai.dto.CreateConversationRequest;
import com.kyawhsan.notiva.ai.dto.NoteChatResponse;
import com.kyawhsan.notiva.ai.dto.SendChatMessageRequest;
import com.kyawhsan.notiva.ai.dto.UpdateConversationRequest;
import com.kyawhsan.notiva.ai.service.AiConversationService;
import com.kyawhsan.notiva.ai.service.NoteChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiConversationController {

    private final AiConversationService aiConversationService;

    private final NoteChatService noteChatService;

    @PostMapping("/notes/{noteId}/ai/conversations")
    public ResponseEntity<ApiResponse<ConversationResponse>> createNoteConversation(
            @PathVariable
            Long noteId,
            @Valid
            @RequestBody(required = false)
            CreateConversationRequest request) {
        ConversationResponse response = aiConversationService.createNoteConversation(noteId,
                request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conversation created successfully", response));
    }

    @PostMapping("/ai/conversations/all-notes")
    public ResponseEntity<ApiResponse<ConversationResponse>> createAllNotesConversation(
            @Valid
            @RequestBody(required = false)
            CreateConversationRequest request) {
        ConversationResponse response = aiConversationService.createAllNotesConversation(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("All-notes conversation created successfully", response));
    }

    @GetMapping("/ai/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getCurrentUserConversations() {
        List<ConversationResponse> response = aiConversationService.getCurrentUserConversations();

        return ResponseEntity
                .ok(ApiResponse.success("Conversations retrieved successfully", response));
    }

    @GetMapping("/ai/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable
            Long conversationId) {
        ConversationResponse response = aiConversationService.getConversation(conversationId);

        return ResponseEntity
                .ok(ApiResponse.success("Conversation retrieved successfully", response));
    }

    @PostMapping("/ai/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<NoteChatResponse>> sendMessage(
            @PathVariable
            Long conversationId,
            @Valid
            @RequestBody
            SendChatMessageRequest request) {
        NoteChatResponse response = noteChatService.sendMessage(conversationId, request);

        return ResponseEntity
                .ok(ApiResponse.success("Chat response generated successfully", response));
    }

    @GetMapping("/ai/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<AiMessageResponse>>> getMessages(
            @PathVariable
            Long conversationId) {
        List<AiMessageResponse> response = noteChatService.getMessages(conversationId);

        return ResponseEntity
                .ok(ApiResponse.success("Conversation messages retrieved successfully", response));
    }

    @PatchMapping("/ai/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> updateConversation(
            @PathVariable
            Long conversationId,
            @Valid
            @RequestBody
            UpdateConversationRequest request) {
        ConversationResponse response = aiConversationService.updateConversation(conversationId,
                request);

        return ResponseEntity
                .ok(ApiResponse.success("Conversation updated successfully", response));
    }

    @DeleteMapping("/ai/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable
            Long conversationId) {
        aiConversationService.deleteConversation(conversationId);

        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully", null));
    }
}
