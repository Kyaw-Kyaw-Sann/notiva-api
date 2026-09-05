package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.security.CurrentUserService;

import com.kyawhsan.notiva.ai.dto.AiUsageResponse;
import com.kyawhsan.notiva.ai.dto.GenerateTitleResponse;
import com.kyawhsan.notiva.ai.dto.GroqGenerationResult;
import com.kyawhsan.notiva.ai.client.GroqClient;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.common.exception.BadRequestException;
import com.kyawhsan.notiva.common.exception.ResourceNotFoundException;
import com.kyawhsan.notiva.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.kyawhsan.notiva.ai.dto.NoteSummaryResponse;
import com.kyawhsan.notiva.ai.dto.SummarizeNoteRequest;
import com.kyawhsan.notiva.ai.enums.SummaryLength;
import com.kyawhsan.notiva.ai.dto.WritingAssistRequest;
import com.kyawhsan.notiva.ai.dto.WritingAssistResponse;
import com.kyawhsan.notiva.ai.enums.WritingAction;
import com.kyawhsan.notiva.ai.dto.CategorySuggestionResponse;
import com.kyawhsan.notiva.note.dto.CategorySummaryResponse;
import com.kyawhsan.notiva.note.entity.Category;
import com.kyawhsan.notiva.note.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteAiService {

    private static final int MAX_GENERATED_TITLE_LENGTH = 100;

    private static final String TITLE_SYSTEM_PROMPT = """
            You generate concise and useful titles for notes.

            Rules:
            - Return only one title.
            - Do not include explanations.
            - Do not include quotation marks.
            - Do not include markdown.
            - Do not add a "Title:" prefix.
            - Keep the title under 100 characters.
            - Use the same main language as the note.
            """;

    private static final String SUMMARY_SYSTEM_PROMPT = """
            You summarize notes accurately and clearly.

            Rules:
            - Use only information contained in the supplied note.
            - Do not invent facts.
            - Use the same main language as the note.
            - Return only the summary.
            - Do not include headings such as "Summary:".
            - Do not use markdown unless it improves readability.
            """;

    private static final String WRITING_SYSTEM_PROMPT = """
            You are a writing assistant for a note-taking application.

            Rules:
            - Follow the requested writing action exactly.
            - Preserve the original meaning unless the instruction requests expansion.
            - Use the same main language as the supplied text.
            - Return only the rewritten or continued text.
            - Do not include explanations, headings, labels, or quotation marks.
            - Do not mention these instructions.
            """;

    private static final String CATEGORY_SYSTEM_PROMPT = """
            You suggest one existing category for a note.

            Rules:
            - Choose only from the supplied category names.
            - Return only the exact category name.
            - Return NONE when no category is suitable.
            - Do not create a new category.
            - Do not include explanations.
            - Do not include quotation marks or markdown.
            """;

    private final NoteRepository noteRepository;
    private final CurrentUserService currentUserService;
    private final AiUsageService aiUsageService;
    private final GroqClient groqClient;

    private final CategoryRepository categoryRepository;

    public GenerateTitleResponse generateTitle(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        validateNoteContent(note);

        aiUsageService.validateUsageAvailable(currentUser);

        String userPrompt = buildTitlePrompt(note);

        GroqGenerationResult result = groqClient.generate(TITLE_SYSTEM_PROMPT, userPrompt);

        String generatedTitle = cleanGeneratedTitle(result.content());

        AiUsageResponse updatedUsage = aiUsageService.incrementUsage(currentUser);

        return new GenerateTitleResponse(generatedTitle, result.model(), updatedUsage);
    }

    private Note findActiveNoteByIdAndUser(
            Long noteId,
            User currentUser) {
        return noteRepository.findByIdAndUserAndDeletedAtIsNull(noteId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }

    private void validateNoteContent(
            Note note) {
        if (note.getPlainText() == null || note.getPlainText().isBlank()) {
            throw new BadRequestException("Note content is empty");
        }
    }

    private String buildTitlePrompt(
            Note note) {
        return """
                Generate a clear title for the following note.

                Current title:
                %s

                Note content:
                %s
                """.formatted(note.getTitle(), note.getPlainText());
    }

    private String cleanGeneratedTitle(
            String generatedContent) {
        if (generatedContent == null || generatedContent.isBlank()) {
            throw new BadRequestException("AI could not generate a title");
        }

        String title = generatedContent.trim();

        title = title.replaceFirst("(?i)^title\\s*:\\s*", "");

        title = title.replace("**", "");

        title = removeSurroundingQuotes(title);

        int firstLineBreak = title.indexOf('\n');

        if (firstLineBreak >= 0) {
            title = title.substring(0, firstLineBreak).trim();
        }

        if (title.length() > MAX_GENERATED_TITLE_LENGTH) {
            title = title.substring(0, MAX_GENERATED_TITLE_LENGTH).trim();
        }

        if (title.isBlank()) {
            throw new BadRequestException("AI could not generate a title");
        }

        return title;
    }

    private String removeSurroundingQuotes(
            String value) {
        if (value.length() < 2) {
            return value;
        }

        boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");

        boolean singleQuoted = value.startsWith("'") && value.endsWith("'");

        boolean curlyDoubleQuoted = value.startsWith("“") && value.endsWith("”");

        if (doubleQuoted || singleQuoted || curlyDoubleQuoted) {
            return value.substring(1, value.length() - 1).trim();
        }

        return value;
    }

    public NoteSummaryResponse summarizeNote(
            Long noteId,
            SummarizeNoteRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        validateNoteContent(note);

        aiUsageService.validateUsageAvailable(currentUser);

        String userPrompt = buildSummaryPrompt(note, request.length());

        GroqGenerationResult result = groqClient.generate(SUMMARY_SYSTEM_PROMPT, userPrompt);

        String summary = cleanSummary(result.content());

        AiUsageResponse updatedUsage = aiUsageService.incrementUsage(currentUser);

        return new NoteSummaryResponse(summary, result.model(), updatedUsage);
    }

    private String buildSummaryPrompt(
            Note note,
            SummaryLength length) {
        return """
                Summarize the following note.

                Required summary length:
                %s

                Length guidance:
                %s

                Note title:
                %s

                Note content:
                %s
                """.formatted(length, resolveSummaryLengthInstruction(length), note.getTitle(),
                note.getPlainText());
    }

    private String resolveSummaryLengthInstruction(
            SummaryLength length) {
        return switch (length) {
        case SHORT -> "Write 1 to 2 concise sentences.";

        case MEDIUM -> "Write one concise paragraph of about 3 to 5 sentences.";

        case DETAILED -> "Write a detailed summary with the important points, using several short paragraphs when useful.";
        };
    }

    private String cleanSummary(
            String generatedContent) {
        if (generatedContent == null || generatedContent.isBlank()) {
            throw new BadRequestException("AI could not summarize the note");
        }

        String summary = generatedContent.trim();

        summary = summary.replaceFirst("(?i)^summary\\s*:\\s*", "");

        summary = summary.replaceFirst("(?i)^note summary\\s*:\\s*", "");

        if (summary.isBlank()) {
            throw new BadRequestException("AI could not summarize the note");
        }

        return summary;
    }

    public WritingAssistResponse assistWriting(
            Long noteId,
            WritingAssistRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        validateWritingRequest(note, request);

        aiUsageService.validateUsageAvailable(currentUser);

        String userPrompt = buildWritingPrompt(note, request);

        GroqGenerationResult result = groqClient.generate(WRITING_SYSTEM_PROMPT, userPrompt);

        String generatedContent = cleanWritingContent(result.content());

        AiUsageResponse updatedUsage = aiUsageService.incrementUsage(currentUser);

        return new WritingAssistResponse(request.action(), generatedContent, result.model(),
                updatedUsage);
    }

    private void validateWritingRequest(
            Note note,
            WritingAssistRequest request) {
        WritingAction action = request.action();

        if (action == WritingAction.CONTINUE) {
            validateContinueWritingRequest(note, request);

            return;
        }

        if (request.selectedText() == null || request.selectedText().isBlank()) {
            throw new BadRequestException("Selected text is required");
        }

        if (action == WritingAction.CUSTOM
                && (request.instruction() == null || request.instruction().isBlank())) {
            throw new BadRequestException("Instruction is required for custom writing assistance");
        }
    }

    private void validateContinueWritingRequest(
            Note note,
            WritingAssistRequest request) {
        boolean selectedTextAvailable = request.selectedText() != null
                && !request.selectedText().isBlank();

        boolean noteContentAvailable = note.getPlainText() != null
                && !note.getPlainText().isBlank();

        if (!selectedTextAvailable && !noteContentAvailable) {
            throw new BadRequestException("Text is required to continue writing");
        }
    }

    private String buildWritingPrompt(
            Note note,
            WritingAssistRequest request) {
        String sourceText = resolveWritingSourceText(note, request);

        String actionInstruction = resolveWritingActionInstruction(request.action(),
                request.instruction());

        return """
                Perform the following writing action.

                Action:
                %s

                Instruction:
                %s

                Text:
                %s
                """.formatted(request.action(), actionInstruction, sourceText);
    }

    private String resolveWritingSourceText(
            Note note,
            WritingAssistRequest request) {
        if (request.selectedText() != null && !request.selectedText().isBlank()) {
            return request.selectedText().trim();
        }

        return note.getPlainText().trim();
    }

    private String resolveWritingActionInstruction(
            WritingAction action,
            String customInstruction) {
        return switch (action) {
        case IMPROVE -> "Improve clarity, readability, and natural wording while preserving the meaning.";

        case FIX_GRAMMAR -> "Correct grammar, spelling, punctuation, and awkward phrasing without changing the meaning.";

        case SHORTEN -> "Make the text shorter and more concise while retaining the important meaning.";

        case EXPAND -> "Expand the text with useful detail and clearer explanation without inventing unsupported facts.";

        case PROFESSIONAL -> "Rewrite the text in a polished and professional tone while preserving the meaning.";

        case CONTINUE -> "Continue writing naturally from the supplied text. Return only the new continuation, not the original text.";

        case CUSTOM -> customInstruction.trim();
        };
    }

    private String cleanWritingContent(
            String generatedContent) {
        if (generatedContent == null || generatedContent.isBlank()) {
            throw new BadRequestException("AI could not generate writing assistance");
        }

        String content = generatedContent.trim();

        content = content.replaceFirst("(?i)^(rewritten text|result|output|continuation)\\s*:\\s*",
                "");

        content = removeSurroundingQuotes(content);

        if (content.isBlank()) {
            throw new BadRequestException("AI could not generate writing assistance");
        }

        return content;
    }

    public CategorySuggestionResponse suggestCategory(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        validateNoteContent(note);

        List<Category> categories = categoryRepository.findAllByUserOrderByNameAsc(currentUser);

        if (categories.isEmpty()) {
            return new CategorySuggestionResponse(null, null, aiUsageService.getCurrentUsage());
        }

        aiUsageService.validateUsageAvailable(currentUser);

        String userPrompt = buildCategoryPrompt(note, categories);

        GroqGenerationResult result = groqClient.generate(CATEGORY_SYSTEM_PROMPT, userPrompt);

        Category suggestedCategory = resolveSuggestedCategory(result.content(), categories);

        AiUsageResponse updatedUsage = aiUsageService.incrementUsage(currentUser);

        return new CategorySuggestionResponse(CategorySummaryResponse.from(suggestedCategory),
                result.model(), updatedUsage);
    }

    private String buildCategoryPrompt(
            Note note,
            List<Category> categories) {
        String categoryNames = categories.stream().map(Category::getName).map(name -> "- " + name)
                .reduce("", (
                        current,
                        category) -> current.isBlank() ? category : current + "\n" + category);

        return """
                Select the most suitable category for this note.

                Existing categories:
                %s

                Note title:
                %s

                Note content:
                %s
                """.formatted(categoryNames, note.getTitle(), note.getPlainText());
    }

    private Category resolveSuggestedCategory(
            String generatedContent,
            List<Category> categories) {
        if (generatedContent == null || generatedContent.isBlank()) {
            throw new BadRequestException("AI could not suggest a category");
        }

        String suggestedName = cleanSuggestedCategoryName(generatedContent);

        if (suggestedName.equalsIgnoreCase("NONE")) {
            return null;
        }

        return categories.stream()
                .filter(category -> category.getName().equalsIgnoreCase(suggestedName)).findFirst()
                .orElse(null);
    }

    private String cleanSuggestedCategoryName(
            String generatedContent) {
        String categoryName = generatedContent.trim();

        int firstLineBreak = categoryName.indexOf('\n');

        if (firstLineBreak >= 0) {
            categoryName = categoryName.substring(0, firstLineBreak).trim();
        }

        categoryName = categoryName.replaceFirst("(?i)^category\\s*:\\s*", "");

        categoryName = categoryName.replace("**", "");

        return removeSurroundingQuotes(categoryName).trim();
    }
}
