package com.kyawhsan.notiva.ai.dto;

public record EmbeddingRebuildResponse(

        int activeNotes,

        int alreadyEmbedded,

        int rebuilt,

        int failed

) {
}
