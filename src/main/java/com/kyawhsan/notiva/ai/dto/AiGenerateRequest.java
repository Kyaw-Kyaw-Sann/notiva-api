package com.kyawhsan.notiva.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiGenerateRequest(

        @NotBlank(message = "Prompt is required")
        @Size(max = 5000, message = "Prompt must not exceed 5000 characters")
        String prompt

) {
}
