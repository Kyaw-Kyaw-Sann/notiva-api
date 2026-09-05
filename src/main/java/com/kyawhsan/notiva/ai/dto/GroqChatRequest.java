package com.kyawhsan.notiva.ai.dto;

import java.util.List;

public record GroqChatRequest(

        String model,

        List<GroqMessage> messages,

        double temperature,

        int max_tokens,

        boolean stream

) {
}
