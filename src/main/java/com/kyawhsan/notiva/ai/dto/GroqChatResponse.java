package com.kyawhsan.notiva.ai.dto;

import java.util.List;

public record GroqChatResponse(

        String id,

        String object,

        long created,

        String model,

        List<GroqChoice> choices,

        GroqUsage usage

) {
}
