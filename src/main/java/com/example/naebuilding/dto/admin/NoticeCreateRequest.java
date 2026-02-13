package com.example.naebuilding.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeCreateRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 4000) String body
) {}
