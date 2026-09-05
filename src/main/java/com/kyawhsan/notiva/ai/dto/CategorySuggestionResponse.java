package com.kyawhsan.notiva.ai.dto;

import com.kyawhsan.notiva.note.dto.CategorySummaryResponse;

public record CategorySuggestionResponse(

        CategorySummaryResponse category,

        String model,

        AiUsageResponse usage

) {
}
