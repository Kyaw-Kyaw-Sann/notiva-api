package com.kyawhsan.notiva.ai.dto;

public record EmbeddingResult(

        float[] vector,

        String model,

        int totalTokens

) {
}
