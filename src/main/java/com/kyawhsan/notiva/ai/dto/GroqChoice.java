package com.kyawhsan.notiva.ai.dto;

public record GroqChoice(

        int index,

        GroqMessage message,

        String finish_reason

) {
}
