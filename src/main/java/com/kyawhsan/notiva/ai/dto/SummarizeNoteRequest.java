package com.kyawhsan.notiva.ai.dto;

import com.kyawhsan.notiva.enums.SummaryLength;
import jakarta.validation.constraints.NotNull;

public record SummarizeNoteRequest(

        @NotNull(message = "Summary length is required")
        SummaryLength length

) {
}
