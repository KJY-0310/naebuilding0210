package com.example.naebuilding.controller;

import com.example.naebuilding.dto.RequestStatusHistoryResponse;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.dto.common.PageResponse;
import com.example.naebuilding.service.RequestStatusHistoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "관리자 API", description = "관리자 전용 - 민원 상태 변경 이력")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/request-status-history")
@RequiredArgsConstructor
public class AdminRequestStatusHistoryController {

    private final RequestStatusHistoryService requestStatusHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RequestStatusHistoryResponse>>> search(
            @RequestParam(required = false) Long requestId,
            @RequestParam(required = false) String actorLoginId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<RequestStatusHistoryResponse> data =
                requestStatusHistoryService.search(requestId, actorLoginId, from, to, pageable);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
