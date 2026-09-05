package com.kyawhsan.notiva.ai.dto;

public record GroqGenerationResult(

        String content,

        String model,

        Integer promptTokens,

        Integer completionTokens,

        Integer totalTokens

) {
}
