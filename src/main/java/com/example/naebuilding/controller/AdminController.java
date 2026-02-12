package com.example.naebuilding.controller;

import com.example.naebuilding.dto.admin.*;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // =========================
    // 사용자 관리
    // =========================

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserListItemDto>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listUsers()));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<Void>> updateRole(
            @PathVariable Long id,
            @RequestBody @Valid UserRoleUpdateRequest req
    ) {
        adminService.updateUserRole(id, req.role());
        return ResponseEntity.ok(ApiResponse.ok("UPDATED", null));
    }

    @PatchMapping("/users/{id}/active")
    public ResponseEntity<ApiResponse<Void>> updateActive(
            @PathVariable Long id,
            @RequestBody @Valid UserActiveUpdateRequest req
    ) {
        adminService.updateUserActive(id, req.active());
        return ResponseEntity.ok(ApiResponse.ok("UPDATED", null));
    }

    // =========================
    // 통계
    // =========================

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getStats()));
    }

    // =========================
    // 시스템 (지금은 "뼈대"만)
    // =========================
    @GetMapping("/system/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("OK", "UP"));
    }
}
