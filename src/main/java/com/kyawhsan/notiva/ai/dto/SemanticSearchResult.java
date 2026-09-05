package com.kyawhsan.notiva.ai.dto;

public record SemanticSearchResult(

        Long chunkId,

        Long noteId,

        String noteTitle,

        int chunkIndex,

        String content,

        double similarity

) {
}
