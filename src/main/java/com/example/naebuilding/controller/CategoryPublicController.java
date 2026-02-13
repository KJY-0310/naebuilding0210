package com.example.naebuilding.controller;

import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listActiveNamesForPublic()));
    }
}
