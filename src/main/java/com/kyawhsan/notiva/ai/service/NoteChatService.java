package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.security.CurrentUserService;

import com.kyawhsan.notiva.ai.dto.AiMessageResponse;
import com.kyawhsan.notiva.ai.dto.AiUsageResponse;
import com.kyawhsan.notiva.ai.dto.GroqGenerationResult;
import com.kyawhsan.notiva.ai.dto.GroqMessage;
import com.kyawhsan.notiva.ai.client.GroqClient;
import com.kyawhsan.notiva.ai.dto.NoteChatResponse;
import com.kyawhsan.notiva.ai.dto.SemanticSearchRequest;
import com.kyawhsan.notiva.ai.dto.SemanticSearchResponse;
import com.kyawhsan.notiva.ai.dto.SemanticSearchResult;
import com.kyawhsan.notiva.ai.dto.SendChatMessageRequest;
import com.kyawhsan.notiva.ai.entity.AiConversation;
import com.kyawhsan.notiva.ai.entity.AiMessage;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.ai.enums.ConversationType;
import com.kyawhsan.notiva.ai.enums.MessageRole;
import com.kyawhsan.notiva.common.exception.BadRequestException;
import com.kyawhsan.notiva.common.exception.ResourceNotFoundException;
import com.kyawhsan.notiva.ai.repository.AiConversationRepository;
import com.kyawhsan.notiva.ai.repository.AiMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteChatService {

    private static final int MAX_NOTE_CONTEXT_LENGTH = 20_000;

    private static final int MAX_RECENT_MESSAGES = 20;

    private static final String NOTE_CHAT_SYSTEM_PROMPT = """
            You are Notiva's note chat assistant.

            Answer using the supplied note as the primary source.

            Rules:
            - Answer clearly and accurately.
            - Use the same main language as the user's message.
            - Do not claim access to notes other than the supplied note.
            - When the answer is not contained in the note, clearly say that the note does not contain enough information.
            - Do not invent facts about the note.
            """;

    private static final String ALL_NOTES_CHAT_SYSTEM_PROMPT = """
            You are Notiva's all-notes assistant.

            Answer using only the supplied note excerpts as the primary source.

            Rules:
            - Answer clearly and accurately.
            - Use the same main language as the user's message.
            - Combine information from multiple supplied notes when useful.
            - Do not claim access to notes that were not supplied.
            - If the supplied excerpts do not contain enough information, clearly say so.
            - Do not invent facts about the user's notes.
            - Mention relevant note titles naturally when helpful.
            """;

    private final AiConversationRepository aiConversationRepository;

    private final AiMessageRepository aiMessageRepository;

    private final CurrentUserService currentUserService;

    private final AiUsageService aiUsageService;

    private final GroqClient groqClient;

    private final SemanticSearchService semanticSearchService;

    public NoteChatResponse sendMessage(
            Long conversationId,
            SendChatMessageRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        AiConversation conversation = findOwnedConversation(conversationId, currentUser);

        aiUsageService.validateUsageAvailable(currentUser);

        String userMessage = request.message().trim();

        List<GroqMessage> groqMessages = switch (conversation.getType()) {
        case NOTE_CHAT -> buildNoteChatMessages(conversation, userMessage);

        case ALL_NOTES_CHAT -> buildAllNotesChatMessages(conversation, userMessage);
        };

        GroqGenerationResult result = groqClient.generate(groqMessages);

        List<AiMessage> savedMessages = saveMessagePair(conversation, userMessage,
                result.content().trim());

        AiUsageResponse updatedUsage = aiUsageService.incrementUsage(currentUser);

        return new NoteChatResponse(toResponse(savedMessages.get(0)),
                toResponse(savedMessages.get(1)), result.model(), updatedUsage);
    }

    @Transactional(readOnly = true)
    public List<AiMessageResponse> getMessages(
            Long conversationId) {
        User currentUser = currentUserService.getCurrentUser();

        AiConversation conversation = findOwnedConversation(conversationId, currentUser);

        return aiMessageRepository.findAllByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(this::toResponse).toList();
    }

    private AiConversation findOwnedConversation(
            Long conversationId,
            User currentUser) {
        return aiConversationRepository.findByIdAndUser(conversationId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
    }

    private List<GroqMessage> buildNoteChatMessages(
            AiConversation conversation,
            String newUserMessage) {
        Note note = validateActiveNote(conversation);

        List<GroqMessage> messages = new ArrayList<>();

        messages.add(new GroqMessage("system", NOTE_CHAT_SYSTEM_PROMPT));

        messages.add(new GroqMessage("system", buildNoteContext(note)));

        addRecentConversationMessages(messages, conversation);

        messages.add(new GroqMessage("user", newUserMessage));

        return messages;
    }

    private List<GroqMessage> buildAllNotesChatMessages(
            AiConversation conversation,
            String newUserMessage) {
        SemanticSearchResponse searchResponse = semanticSearchService
                .search(new SemanticSearchRequest(newUserMessage));

        List<GroqMessage> messages = new ArrayList<>();

        messages.add(new GroqMessage("system", ALL_NOTES_CHAT_SYSTEM_PROMPT));

        messages.add(new GroqMessage("system", buildAllNotesContext(searchResponse.results())));

        addRecentConversationMessages(messages, conversation);

        messages.add(new GroqMessage("user", newUserMessage));

        return messages;
    }

    private Note validateActiveNote(
            AiConversation conversation) {
        if (conversation.getType() != ConversationType.NOTE_CHAT) {
            throw new ResourceNotFoundException("Conversation not found");
        }

        Note note = conversation.getNote();

        if (note == null || note.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Note not found");
        }

        if (note.getPlainText() == null || note.getPlainText().isBlank()) {
            throw new BadRequestException("Note content is empty");
        }

        return note;
    }

    private void addRecentConversationMessages(
            List<GroqMessage> groqMessages,
            AiConversation conversation) {
        List<AiMessage> recentMessages = aiMessageRepository
                .findTop20ByConversationOrderByCreatedAtDesc(conversation);

        Collections.reverse(recentMessages);

        for (AiMessage message : recentMessages) {
            groqMessages
                    .add(new GroqMessage(resolveGroqRole(message.getRole()), message.getContent()));
        }
    }

    private String buildNoteContext(
            Note note) {
        String noteContent = truncateNoteContent(note.getPlainText());

        String noteTitle = note.getTitle() == null || note.getTitle().isBlank() ? "Untitled Note"
                : note.getTitle().trim();

        return """
                The following is the only note available for this conversation.

                Note title:
                %s

                Note content:
                %s
                """.formatted(noteTitle, noteContent);
    }

    private String buildAllNotesContext(
            List<SemanticSearchResult> results) {
        if (results == null || results.isEmpty()) {
            return """
                    No relevant note excerpts were found for the user's question.

                    Tell the user that their active notes do not contain enough relevant information.
                    """;
        }

        StringBuilder context = new StringBuilder("""
                The following note excerpts were retrieved from the user's active notes.

                Use only these excerpts as note-based context.

                """);

        for (int index = 0; index < results.size(); index++) {

            SemanticSearchResult result = results.get(index);

            context.append("""
                    --- Note excerpt %d ---
                    Note title: %s
                    Note ID: %d
                    Chunk index: %d
                    Similarity: %.4f

                    Content:
                    %s

                    """.formatted(index + 1, resolveNoteTitle(result.noteTitle()), result.noteId(),
                    result.chunkIndex(), result.similarity(), result.content()));
        }

        return context.toString();
    }

    private String resolveNoteTitle(
            String title) {
        if (title == null || title.isBlank()) {
            return "Untitled Note";
        }

        return title.trim();
    }

    private String truncateNoteContent(
            String plainText) {
        String content = plainText.trim();

        if (content.length() <= MAX_NOTE_CONTEXT_LENGTH) {
            return content;
        }

        return content.substring(0, MAX_NOTE_CONTEXT_LENGTH);
    }

    private String resolveGroqRole(
            MessageRole role) {
        return switch (role) {
        case USER -> "user";
        case ASSISTANT -> "assistant";
        };
    }

    @Transactional
    protected List<AiMessage> saveMessagePair(
            AiConversation conversation,
            String userContent,
            String assistantContent) {
        AiMessage userMessage = new AiMessage();

        userMessage.setRole(MessageRole.USER);

        userMessage.setContent(userContent);

        userMessage.setConversation(conversation);

        AiMessage assistantMessage = new AiMessage();

        assistantMessage.setRole(MessageRole.ASSISTANT);

        assistantMessage.setContent(assistantContent);

        assistantMessage.setConversation(conversation);

        return aiMessageRepository.saveAll(List.of(userMessage, assistantMessage));
    }

    private AiMessageResponse toResponse(
            AiMessage message) {
        return new AiMessageResponse(message.getId(), message.getRole(), message.getContent(),
                message.getCreatedAt());
    }
}
