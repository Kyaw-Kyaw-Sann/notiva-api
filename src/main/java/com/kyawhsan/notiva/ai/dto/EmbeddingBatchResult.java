package com.kyawhsan.notiva.ai.dto;

import java.util.List;

public record EmbeddingBatchResult(

        List<float[]> vectors,

        String model,

        int totalTokens

) {
}
