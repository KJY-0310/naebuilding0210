package com.example.naebuilding.dto;

public record RequestImageDto(
        Long imageId,
        String imageUrl,
        Integer sortOrder
) {}

