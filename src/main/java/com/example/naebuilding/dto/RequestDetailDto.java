package com.example.naebuilding.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RequestDetailDto(
        Long requestId,
        String title,
        String content,
        String category,
        String location,
        String status,
        String writerNickname,
        String adminNote,
        LocalDateTime createdAt,
        List<RequestImageDto> images
) {}
