package com.kyawhsan.notiva.ai.dto;

import java.util.List;

public record VoyageEmbeddingData(

        String object,

        List<Double> embedding,

        int index

) {
}
