package com.kyawhsan.notiva.ai.dto;

import com.kyawhsan.notiva.enums.MessageRole;

import java.time.LocalDateTime;

public record AiMessageResponse(

        Long id,

        MessageRole role,

        String content,

        LocalDateTime createdAt

) {
}
