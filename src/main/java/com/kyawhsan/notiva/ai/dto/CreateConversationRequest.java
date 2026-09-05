package com.kyawhsan.notiva.ai.dto;

import jakarta.validation.constraints.Size;

public record CreateConversationRequest(

        @Size(max = 100, message = "Conversation title must not exceed 100 characters")
        String title

) {
}
