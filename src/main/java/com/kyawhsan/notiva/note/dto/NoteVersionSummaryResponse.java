package com.kyawhsan.notiva.note.dto;

import com.kyawhsan.notiva.note.entity.NoteVersion;

import java.time.LocalDateTime;

public record NoteVersionSummaryResponse(

        Long id,

        String title,

        String plainTextPreview,

        LocalDateTime createdAt

) {

    private static final int PREVIEW_LENGTH = 120;

    public static NoteVersionSummaryResponse from(
            NoteVersion version) {
        return new NoteVersionSummaryResponse(version.getId(), version.getTitle(),
                createPreview(version.getPlainText()), version.getCreatedAt());
    }

    private static String createPreview(
            String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return "";
        }

        String normalizedText = plainText.trim().replaceAll("\\s+", " ");

        if (normalizedText.length() <= PREVIEW_LENGTH) {
            return normalizedText;
        }

        return normalizedText.substring(0, PREVIEW_LENGTH) + "...";
    }
}