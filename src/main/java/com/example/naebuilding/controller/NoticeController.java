package com.example.naebuilding.controller;

import com.example.naebuilding.dto.notice.NoticeListItemResponse;
import com.example.naebuilding.dto.notice.NoticeResponse;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.service.NoticeReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeReadService noticeReadService;

    // ✅ 공지 목록 (사용자용: 목록에서는 body 전체 안 내려도 됨)
    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeListItemResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("OK", noticeReadService.list()));
    }

    // ✅ 공지 상세
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok("OK", noticeReadService.detail(id))
        );
    }
}
