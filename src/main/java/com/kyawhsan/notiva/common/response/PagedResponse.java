package com.kyawhsan.notiva.common.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(

        List<T> content,

        int page,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last

) {

    public static <E, T> PagedResponse<T> from(
            Page<E> source,
            Function<E, T> mapper) {
        List<T> content = source.getContent().stream().map(mapper).toList();

        return new PagedResponse<>(content, source.getNumber(), source.getSize(),
                source.getTotalElements(), source.getTotalPages(), source.isFirst(),
                source.isLast());
    }
}