package com.example.naebuilding.dto.admin;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long noticeId,
        String title,
        String body,
        LocalDateTime createdAt,
        Long createdBy,
        String createdByLoginId
) {}