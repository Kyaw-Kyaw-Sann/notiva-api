package com.kyawhsan.notiva.note.dto;

import com.kyawhsan.notiva.entity.NoteVersion;

import java.time.LocalDateTime;

public record NoteVersionResponse(

        Long id,

        Long noteId,

        String title,

        String contentJson,

        String plainText,

        LocalDateTime createdAt

) {

    public static NoteVersionResponse from(
            NoteVersion version) {
        return new NoteVersionResponse(version.getId(), version.getNote().getId(),
                version.getTitle(), version.getContentJson(), version.getPlainText(),
                version.getCreatedAt());
    }
}