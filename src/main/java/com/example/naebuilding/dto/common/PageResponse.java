package com.example.naebuilding.dto.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrev,
        String sort
) {
    public static <T> PageResponse<T> from(Page<T> pageData) {
        return new PageResponse<>(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.hasNext(),
                pageData.hasPrevious(),
                sortToString(pageData.getSort())
        );
    }

    private static String sortToString(Sort sort) {
        if (sort == null || sort.isUnsorted()) return "UNSORTED";
        return sort.stream()
                .map(o -> o.getProperty() + "," + o.getDirection().name().toLowerCase())
                .reduce((a, b) -> a + " | " + b)
                .orElse("UNSORTED");
    }
}
