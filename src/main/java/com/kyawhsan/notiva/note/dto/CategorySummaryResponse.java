package com.kyawhsan.notiva.note.dto;

import com.kyawhsan.notiva.entity.Category;

public record CategorySummaryResponse(
        Long id,
        String name) {

    public static CategorySummaryResponse from(
            Category category) {
        if (category == null) {
            return null;
        }

        return new CategorySummaryResponse(category.getId(), category.getName());
    }
}