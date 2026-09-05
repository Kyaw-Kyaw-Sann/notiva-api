package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.security.CurrentUserService;

import com.kyawhsan.notiva.ai.dto.EmbeddingResult;
import com.kyawhsan.notiva.ai.dto.SemanticSearchRequest;
import com.kyawhsan.notiva.ai.dto.SemanticSearchResponse;
import com.kyawhsan.notiva.ai.dto.SemanticSearchResult;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.ai.repository.NoteChunkJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    private static final int FINAL_RESULT_LIMIT = 5;

    private static final int CANDIDATE_RESULT_LIMIT = 20;

    private static final int MAX_CHUNKS_PER_NOTE = 2;

    private static final double MINIMUM_SIMILARITY = 0.35;

    private final EmbeddingClient embeddingClient;

    private final NoteChunkJdbcRepository noteChunkJdbcRepository;

    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public SemanticSearchResponse search(
            SemanticSearchRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        String question = request.question().trim();

        EmbeddingResult queryEmbedding = embeddingClient.embedQuery(question);

        List<SemanticSearchResult> candidates = noteChunkJdbcRepository.findMostSimilarActiveChunks(
                currentUser.getId(), queryEmbedding.vector(), CANDIDATE_RESULT_LIMIT);

        List<SemanticSearchResult> results = selectRelevantResults(candidates);

        return new SemanticSearchResponse(question, results.size(), results);
    }

    private List<SemanticSearchResult> selectRelevantResults(
            List<SemanticSearchResult> candidates) {
        List<SemanticSearchResult> selected = new ArrayList<>();

        Map<Long, Integer> resultCountByNote = new HashMap<>();

        for (SemanticSearchResult candidate : candidates) {

            if (candidate.similarity() < MINIMUM_SIMILARITY) {
                continue;
            }

            int noteResultCount = resultCountByNote.getOrDefault(candidate.noteId(), 0);

            if (noteResultCount >= MAX_CHUNKS_PER_NOTE) {
                continue;
            }

            selected.add(candidate);

            resultCountByNote.put(candidate.noteId(), noteResultCount + 1);

            if (selected.size() >= FINAL_RESULT_LIMIT) {
                break;
            }
        }

        return selected;
    }
}
