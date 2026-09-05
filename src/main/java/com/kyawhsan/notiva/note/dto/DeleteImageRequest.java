package com.kyawhsan.notiva.note.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteImageRequest(

        @NotBlank(message = "Image public ID is required")
        String publicId) {
}