package com.example.naebuilding.controller;

import com.example.naebuilding.dto.admin.CategoryCreateRequest;
import com.example.naebuilding.dto.admin.CategoryResponse;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listForAdmin()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@RequestBody @Valid CategoryCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("CREATED", categoryService.create(req)));
    }

    // ✅ 비활성(소프트 삭제)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable("id") Long id) {
        categoryService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok("DEACTIVATED", null));
    }

    // ✅ 활성(복구)
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable("id") Long id) {
        categoryService.activate(id);
        return ResponseEntity.ok(ApiResponse.ok("ACTIVATED", null));
    }

    // ✅ 하드삭제(실제 삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHard(@PathVariable("id") Long id) {
        categoryService.deleteHard(id);
        return ResponseEntity.ok(ApiResponse.ok("DELETED", null));
    }
}
