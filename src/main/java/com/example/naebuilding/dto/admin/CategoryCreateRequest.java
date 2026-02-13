package com.example.naebuilding.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        String name
) {}
