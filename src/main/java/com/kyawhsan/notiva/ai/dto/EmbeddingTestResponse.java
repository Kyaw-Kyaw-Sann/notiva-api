package com.kyawhsan.notiva.ai.dto;

import java.util.List;

public record EmbeddingTestResponse(

        String model,

        int dimension,

        int totalTokens,

        String pgVectorVersion,

        List<Float> preview

) {
}
