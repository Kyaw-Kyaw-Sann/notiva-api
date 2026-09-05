package com.kyawhsan.notiva.note.dto;

import com.kyawhsan.notiva.note.entity.Category;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CategoryResponse from(
            Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getCreatedAt(),
                category.getUpdatedAt());
    }
}