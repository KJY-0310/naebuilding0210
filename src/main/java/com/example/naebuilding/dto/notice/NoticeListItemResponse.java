package com.example.naebuilding.dto.notice;

import java.time.LocalDateTime;

public record NoticeListItemResponse(
        Long noticeId,
        String title,
        String preview,
        LocalDateTime createdAt,
        String createdByLoginId
) {}
