package com.kyawhsan.notiva.ai.dto;

import java.util.List;

public record VoyageEmbeddingRequest(

        List<String> input,

        String model,

        String input_type,

        boolean truncation,

        int output_dimension,

        String output_dtype

) {
}
