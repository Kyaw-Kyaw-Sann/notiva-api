package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.security.CurrentUserService;

import com.kyawhsan.notiva.ai.dto.ConversationNoteResponse;
import com.kyawhsan.notiva.ai.dto.ConversationResponse;
import com.kyawhsan.notiva.ai.dto.CreateConversationRequest;
import com.kyawhsan.notiva.ai.dto.UpdateConversationRequest;
import com.kyawhsan.notiva.ai.entity.AiConversation;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.ai.enums.ConversationType;
import com.kyawhsan.notiva.common.exception.ResourceNotFoundException;
import com.kyawhsan.notiva.ai.repository.AiConversationRepository;
import com.kyawhsan.notiva.ai.repository.AiMessageRepository;
import com.kyawhsan.notiva.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiConversationService {

    private static final int MAX_TITLE_LENGTH = 100;

    private static final String DEFAULT_ALL_NOTES_TITLE = "Chat with all notes";

    private final AiConversationRepository aiConversationRepository;

    private final NoteRepository noteRepository;

    private final CurrentUserService currentUserService;

    private final AiMessageRepository aiMessageRepository;

    @Transactional
    public ConversationResponse createNoteConversation(
            Long noteId,
            CreateConversationRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNote(noteId, currentUser);

        AiConversation conversation = new AiConversation();

        conversation.setTitle(resolveNoteConversationTitle(request, note));

        conversation.setType(ConversationType.NOTE_CHAT);

        conversation.setUser(currentUser);
        conversation.setNote(note);

        AiConversation savedConversation = aiConversationRepository.save(conversation);

        return toResponse(savedConversation);
    }

    @Transactional
    public ConversationResponse createAllNotesConversation(
            CreateConversationRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        AiConversation conversation = new AiConversation();

        conversation.setTitle(resolveAllNotesConversationTitle(request));

        conversation.setType(ConversationType.ALL_NOTES_CHAT);

        conversation.setUser(currentUser);

        conversation.setNote(null);

        AiConversation savedConversation = aiConversationRepository.save(conversation);

        return toResponse(savedConversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getCurrentUserConversations() {
        User currentUser = currentUserService.getCurrentUser();

        return aiConversationRepository.findAllByUserOrderByUpdatedAtDesc(currentUser).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(
            Long conversationId) {
        User currentUser = currentUserService.getCurrentUser();

        AiConversation conversation = findOwnedConversation(conversationId, currentUser);

        return toResponse(conversation);
    }

    @Transactional
    public ConversationResponse updateConversation(
            Long conversationId,
            UpdateConversationRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        AiConversation conversation = findOwnedConversation(conversationId, currentUser);

        conversation.setTitle(request.title().trim());

        AiConversation savedConversation = aiConversationRepository.save(conversation);

        return toResponse(savedConversation);
    }

    @Transactional
    public void deleteConversation(
            Long conversationId) {
        User currentUser = currentUserService.getCurrentUser();

        AiConversation conversation = findOwnedConversation(conversationId, currentUser);

        aiMessageRepository.deleteAllByConversation(conversation);

        aiConversationRepository.delete(conversation);
    }

    private Note findActiveNote(
            Long noteId,
            User currentUser) {
        return noteRepository.findByIdAndUserAndDeletedAtIsNull(noteId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }

    private AiConversation findOwnedConversation(
            Long conversationId,
            User currentUser) {
        return aiConversationRepository.findByIdAndUser(conversationId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
    }

    private String resolveNoteConversationTitle(
            CreateConversationRequest request,
            Note note) {
        String requestedTitle = resolveRequestedTitle(request);

        if (requestedTitle != null) {
            return requestedTitle;
        }

        String noteTitle = note.getTitle();

        if (noteTitle == null || noteTitle.isBlank()) {
            return "Note Chat";
        }

        return limitTitle("Chat with " + noteTitle.trim());
    }

    private String resolveAllNotesConversationTitle(
            CreateConversationRequest request) {
        String requestedTitle = resolveRequestedTitle(request);

        if (requestedTitle != null) {
            return requestedTitle;
        }

        return DEFAULT_ALL_NOTES_TITLE;
    }

    private String resolveRequestedTitle(
            CreateConversationRequest request) {
        if (request == null || request.title() == null || request.title().isBlank()) {
            return null;
        }

        return limitTitle(request.title().trim());
    }

    private String limitTitle(
            String title) {
        if (title.length() <= MAX_TITLE_LENGTH) {
            return title;
        }

        return title.substring(0, MAX_TITLE_LENGTH).trim();
    }

    private ConversationResponse toResponse(
            AiConversation conversation) {
        ConversationNoteResponse noteResponse = null;

        if (conversation.getNote() != null) {
            noteResponse = new ConversationNoteResponse(conversation.getNote().getId(),
                    conversation.getNote().getTitle());
        }

        return new ConversationResponse(conversation.getId(), conversation.getTitle(),
                conversation.getType(), noteResponse, conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }
}
