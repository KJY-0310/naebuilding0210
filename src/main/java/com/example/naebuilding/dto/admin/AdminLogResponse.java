package com.example.naebuilding.dto.admin;

import java.time.LocalDateTime;

public record AdminLogResponse(
        Long logId,
        LocalDateTime createdAt,
        Long actorUserId,
        String actorLoginId,
        String action,
        String targetType,
        Long targetId,
        String message,
        String ip
) {}
