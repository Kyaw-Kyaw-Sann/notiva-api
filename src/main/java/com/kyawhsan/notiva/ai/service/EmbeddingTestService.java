package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.common.persistence.PgVectorService;

import com.kyawhsan.notiva.ai.dto.EmbeddingResult;
import com.kyawhsan.notiva.ai.dto.EmbeddingTestRequest;
import com.kyawhsan.notiva.ai.dto.EmbeddingTestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingTestService {

    private static final int PREVIEW_SIZE = 5;

    private final EmbeddingClient embeddingClient;
    private final PgVectorService pgVectorService;

    public EmbeddingTestResponse testDocumentEmbedding(
            EmbeddingTestRequest request) {
        String pgVectorVersion = pgVectorService.getInstalledVersion();

        EmbeddingResult result = embeddingClient.embedDocument(request.text());

        return new EmbeddingTestResponse(result.model(), result.vector().length,
                result.totalTokens(), pgVectorVersion, createPreview(result.vector()));
    }

    private List<Float> createPreview(
            float[] vector) {
        int size = Math.min(PREVIEW_SIZE, vector.length);

        List<Float> preview = new ArrayList<>(size);

        for (int index = 0; index < size; index++) {
            preview.add(vector[index]);
        }

        return preview;
    }
}
