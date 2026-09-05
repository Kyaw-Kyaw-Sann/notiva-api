package com.kyawhsan.notiva.ai.dto;

public record GenerateTitleResponse(

        String title,

        String model,

        AiUsageResponse usage

) {
}
