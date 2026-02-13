package com.example.naebuilding.dto.admin;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long categoryId,
        String name,
        boolean active,
        LocalDateTime createdAt
) {}
