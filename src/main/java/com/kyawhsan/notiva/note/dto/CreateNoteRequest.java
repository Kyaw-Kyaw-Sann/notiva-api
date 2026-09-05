package com.kyawhsan.notiva.note.dto;

import com.kyawhsan.notiva.enums.NoteBackgroundColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNoteRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Content JSON is required")
        String contentJson,

        @NotNull(message = "Plain text is required")
        String plainText,

        @NotNull(message = "Background color is required")
        NoteBackgroundColor backgroundColor,

        Long categoryId) {
}