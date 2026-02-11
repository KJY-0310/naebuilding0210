package com.example.naebuilding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestCreateRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 2000) String content,
        @NotBlank @Size(max = 50) String category,
        @NotBlank @Size(max = 100) String location
) {}
