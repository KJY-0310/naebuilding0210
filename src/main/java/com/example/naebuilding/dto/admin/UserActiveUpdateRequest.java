package com.example.naebuilding.dto.admin;

import jakarta.validation.constraints.NotNull;

public record UserActiveUpdateRequest(
        @NotNull(message = "active는 필수입니다.")
        Boolean active
) {}
