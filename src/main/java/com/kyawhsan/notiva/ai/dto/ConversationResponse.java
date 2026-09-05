package com.kyawhsan.notiva.ai.dto;

import com.kyawhsan.notiva.enums.ConversationType;

import java.time.LocalDateTime;

public record ConversationResponse(

        Long id,

        String title,

        ConversationType type,

        ConversationNoteResponse note,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
