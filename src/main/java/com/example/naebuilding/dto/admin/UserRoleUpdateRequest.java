package com.example.naebuilding.dto.admin;

import com.example.naebuilding.domain.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
        @NotNull(message = "role은 필수입니다.")
        Role role
) {}
