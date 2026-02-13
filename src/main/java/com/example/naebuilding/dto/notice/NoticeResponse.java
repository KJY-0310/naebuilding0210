package com.example.naebuilding.dto.notice;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long noticeId,
        String title,
        String body,
        LocalDateTime createdAt,
        Long createdBy,
        String createdByLoginId
) {}
