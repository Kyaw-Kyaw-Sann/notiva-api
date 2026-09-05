package com.kyawhsan.notiva.ai.controller;

import com.kyawhsan.notiva.common.response.ApiResponse;
import com.kyawhsan.notiva.ai.dto.CategorySuggestionResponse;
import com.kyawhsan.notiva.ai.dto.GenerateTitleResponse;
import com.kyawhsan.notiva.ai.dto.NoteSummaryResponse;
import com.kyawhsan.notiva.ai.dto.SummarizeNoteRequest;
import com.kyawhsan.notiva.ai.dto.WritingAssistRequest;
import com.kyawhsan.notiva.ai.dto.WritingAssistResponse;
import com.kyawhsan.notiva.ai.service.NoteAiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes/{noteId}/ai")
@RequiredArgsConstructor
public class NoteAiController {

    private final NoteAiService noteAiService;

    @PostMapping("/generate-title")
    public ResponseEntity<ApiResponse<GenerateTitleResponse>> generateTitle(
            @PathVariable
            Long noteId) {
        GenerateTitleResponse response = noteAiService.generateTitle(noteId);

        return ResponseEntity
                .ok(ApiResponse.success("Note title generated successfully", response));
    }

    @PostMapping("/summarize")
    public ResponseEntity<ApiResponse<NoteSummaryResponse>> summarizeNote(
            @PathVariable
            Long noteId,
            @Valid
            @RequestBody
            SummarizeNoteRequest request) {
        NoteSummaryResponse response = noteAiService.summarizeNote(noteId, request);

        return ResponseEntity.ok(ApiResponse.success("Note summarized successfully", response));
    }

    @PostMapping("/write")
    public ResponseEntity<ApiResponse<WritingAssistResponse>> assistWriting(
            @PathVariable
            Long noteId,
            @Valid
            @RequestBody
            WritingAssistRequest request) {
        WritingAssistResponse response = noteAiService.assistWriting(noteId, request);

        return ResponseEntity
                .ok(ApiResponse.success("Writing assistance generated successfully", response));
    }

    @PostMapping("/suggest-category")
    public ResponseEntity<ApiResponse<CategorySuggestionResponse>> suggestCategory(
            @PathVariable
            Long noteId) {
        CategorySuggestionResponse response = noteAiService.suggestCategory(noteId);

        return ResponseEntity
                .ok(ApiResponse.success("Category suggestion generated successfully", response));
    }
}
