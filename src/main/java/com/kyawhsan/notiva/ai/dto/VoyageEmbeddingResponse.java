package com.kyawhsan.notiva.ai.dto;

import java.util.List;

public record VoyageEmbeddingResponse(

        String object,

        List<VoyageEmbeddingData> data,

        String model,

        VoyageEmbeddingUsage usage

) {
}
