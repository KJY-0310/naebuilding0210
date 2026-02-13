package com.example.naebuilding.dto;

import java.time.LocalDateTime;

public record RequestStatusHistoryResponse(
        Long historyId,
        Long requestId,
        String beforeStatus,
        String afterStatus,
        Long changedByUserId,
        String changedByLoginId,
        String ip,
        String userAgent,
        LocalDateTime createdAt
) {}
