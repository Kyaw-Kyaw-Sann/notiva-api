package com.kyawhsan.notiva.ai.controller;

import com.kyawhsan.notiva.common.response.ApiResponse;
import com.kyawhsan.notiva.ai.dto.EmbeddingRebuildResponse;
import com.kyawhsan.notiva.ai.dto.EmbeddingTestRequest;
import com.kyawhsan.notiva.ai.dto.EmbeddingTestResponse;
import com.kyawhsan.notiva.ai.dto.SemanticSearchRequest;
import com.kyawhsan.notiva.ai.dto.SemanticSearchResponse;
import com.kyawhsan.notiva.ai.service.EmbeddingRebuildService;
import com.kyawhsan.notiva.ai.service.EmbeddingTestService;
import com.kyawhsan.notiva.ai.service.NoteEmbeddingService;
import com.kyawhsan.notiva.ai.service.SemanticSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/embeddings")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingTestService embeddingTestService;

    private final NoteEmbeddingService noteEmbeddingService;

    private final SemanticSearchService semanticSearchService;

    private final EmbeddingRebuildService embeddingRebuildService;

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<EmbeddingTestResponse>> testEmbedding(
            @Valid
            @RequestBody
            EmbeddingTestRequest request) {
        EmbeddingTestResponse response = embeddingTestService.testDocumentEmbedding(request);

        return ResponseEntity.ok(ApiResponse.success("Embedding generated successfully", response));
    }

    @GetMapping("/notes/{noteId}/count")
    public ResponseEntity<ApiResponse<Long>> getNoteChunkCount(
            @PathVariable
            Long noteId) {
        long count = noteEmbeddingService.countByNoteId(noteId);

        return ResponseEntity
                .ok(ApiResponse.success("Note chunk count retrieved successfully", count));
    }

    @PostMapping("/semantic-search")
    public ResponseEntity<ApiResponse<SemanticSearchResponse>> semanticSearch(
            @Valid
            @RequestBody
            SemanticSearchRequest request) {
        SemanticSearchResponse response = semanticSearchService.search(request);

        return ResponseEntity
                .ok(ApiResponse.success("Semantic search completed successfully", response));
    }

    @PostMapping("/rebuild-missing")
    public ResponseEntity<ApiResponse<EmbeddingRebuildResponse>> rebuildMissingEmbeddings() {
        EmbeddingRebuildResponse response = embeddingRebuildService.rebuildMissingEmbeddings();

        return ResponseEntity
                .ok(ApiResponse.success("Missing embeddings rebuild completed", response));
    }
}
