package com.example.naebuilding.controller;

import com.example.naebuilding.config.SecurityUtil;
import com.example.naebuilding.dto.admin.NoticeCreateRequest;
import com.example.naebuilding.dto.admin.NoticeResponse;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> list() {
        return ResponseEntity.ok(
                ApiResponse.ok("OK", noticeService.list())
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> create(
            @RequestBody @Valid NoticeCreateRequest req,
            HttpServletRequest http
    ) {
        Long actorId = securityUtil.currentUserId();
        String actorLoginId = securityUtil.currentLoginId();

        String ip = getClientIp(http);
        String ua = http.getHeader("User-Agent");

        NoticeResponse saved =
                noticeService.create(req, actorId, actorLoginId, ip, ua);

        return ResponseEntity.ok(
                ApiResponse.ok("CREATED", saved)
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank())
            return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
