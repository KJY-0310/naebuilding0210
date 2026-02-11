package com.example.naebuilding.dto;

import com.example.naebuilding.domain.RequestStatus;
import java.time.LocalDateTime;

public record RequestListItemDto(
        Long requestId,
        String title,
        String category,
        String location,
        RequestStatus status,
        String writerNickname,
        LocalDateTime createdAt
) {}