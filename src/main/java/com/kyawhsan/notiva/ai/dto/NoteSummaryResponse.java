package com.kyawhsan.notiva.ai.dto;

public record NoteSummaryResponse(

        String summary,

        String model,

        AiUsageResponse usage

) {
}
