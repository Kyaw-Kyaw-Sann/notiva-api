package com.kyawhsan.notiva.note.dto;

import com.kyawhsan.notiva.enums.NoteBackgroundColor;
import com.kyawhsan.notiva.enums.NoteSort;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteSearchRequest {

    private String query;

    private Long categoryId;

    private Boolean uncategorized;

    private NoteBackgroundColor backgroundColor;

    private Boolean pinned;

    private Boolean favorite;

    private NoteSort sort = NoteSort.UPDATED_DESC;

    @Min(value = 0, message = "Page must be zero or greater")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1") @Max(value = 50, message = "Page size must not exceed 50")
    private int size = 12;
}