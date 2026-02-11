package com.example.naebuilding.dto;

import jakarta.validation.constraints.Size;

public record AdminNoteUpdateRequest(
        @Size(max = 1000) String adminNote
) {}