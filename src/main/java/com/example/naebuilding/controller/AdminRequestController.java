package com.example.naebuilding.controller;

import com.example.naebuilding.dto.AdminNoteUpdateRequest;
import com.example.naebuilding.dto.RequestStatusUpdateRequest;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 API", description = "관리자 전용 민원 관리 기능")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/requests")
@RequiredArgsConstructor
public class AdminRequestController {

    private final RequestService requestService;

    @Operation(summary = "민원 상태 변경", description = "ADMIN만 가능")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid RequestStatusUpdateRequest req,
            HttpServletRequest httpReq
    ) {
        requestService.updateStatus(id, req.status(), httpReq);
        return ResponseEntity.ok(ApiResponse.ok("UPDATED", null));
    }

    @Operation(summary = "관리자 메모 수정", description = "ADMIN만 가능")
    @PatchMapping("/{id}/admin-note")
    public ResponseEntity<ApiResponse<Void>> updateAdminNote(
            @PathVariable Long id,
            @RequestBody @Valid AdminNoteUpdateRequest req
    ) {
        requestService.updateAdminNote(id, req.adminNote());
        return ResponseEntity.ok(ApiResponse.ok("UPDATED", null));
    }
}
