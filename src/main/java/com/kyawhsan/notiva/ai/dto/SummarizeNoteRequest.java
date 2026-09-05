package com.kyawhsan.notiva.ai.dto;

import com.kyawhsan.notiva.ai.enums.SummaryLength;
import jakarta.validation.constraints.NotNull;

public record SummarizeNoteRequest(

        @NotNull(message = "Summary length is required")
        SummaryLength length

) {
}
