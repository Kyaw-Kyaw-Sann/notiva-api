package com.kyawhsan.notiva.ai.dto;

public record GroqUsage(

        int prompt_tokens,

        int completion_tokens,

        int total_tokens

) {
}
