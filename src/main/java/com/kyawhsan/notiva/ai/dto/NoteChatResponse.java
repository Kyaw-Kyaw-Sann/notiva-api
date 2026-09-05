package com.kyawhsan.notiva.ai.dto;

public record NoteChatResponse(

        AiMessageResponse userMessage,

        AiMessageResponse assistantMessage,

        String model,

        AiUsageResponse usage

) {
}
