package com.kyawhsan.notiva.ai.dto;

import java.util.List;

public record SemanticSearchResponse(

        String question,

        int resultCount,

        List<SemanticSearchResult> results

) {
}
