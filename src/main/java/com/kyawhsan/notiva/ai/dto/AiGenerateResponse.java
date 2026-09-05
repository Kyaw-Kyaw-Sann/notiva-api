package com.kyawhsan.notiva.ai.dto;

public record AiGenerateResponse(

        String content,

        String model,

        Integer promptTokens,

        Integer completionTokens,

        Integer totalTokens,

        AiUsageResponse usage

) {
}
