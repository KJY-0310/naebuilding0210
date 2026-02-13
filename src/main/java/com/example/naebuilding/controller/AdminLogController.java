package com.example.naebuilding.controller;

import com.example.naebuilding.dto.admin.AdminLogResponse;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.service.AdminLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.naebuilding.dto.common.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final AdminLogService adminLogService;

    // 예: /api/admin/logs?from=2026-02-01T00:00:00&to=2026-02-13T23:59:59&action=NOTICE_CREATE&keyword=전기
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminLogResponse>>> search(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actorLoginId,
            @RequestParam(required = false) String keyword,

            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AdminLogResponse> page = adminLogService.search(from, to, action, actorLoginId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }
}
