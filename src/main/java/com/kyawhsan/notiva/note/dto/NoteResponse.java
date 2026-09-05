package com.kyawhsan.notiva.note.dto;

import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.note.enums.NoteBackgroundColor;

import java.time.LocalDateTime;

public record NoteResponse(

        Long id,

        String title,

        String contentJson,

        String plainText,

        NoteBackgroundColor backgroundColor,

        boolean pinned,

        boolean favorite,

        CategorySummaryResponse category,

        LocalDateTime deletedAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {

    public static NoteResponse from(
            Note note) {
        return new NoteResponse(note.getId(), note.getTitle(), note.getContentJson(),
                note.getPlainText(), note.getBackgroundColor(), note.isPinned(), note.isFavorite(),
                CategorySummaryResponse.from(note.getCategory()), note.getDeletedAt(),
                note.getCreatedAt(), note.getUpdatedAt());
    }
}