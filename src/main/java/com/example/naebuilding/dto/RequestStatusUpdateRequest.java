package com.example.naebuilding.dto;

import com.example.naebuilding.domain.RequestStatus;
import jakarta.validation.constraints.NotNull;

public record RequestStatusUpdateRequest(
        @NotNull RequestStatus status
) {}
