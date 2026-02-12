package com.example.naebuilding.dto.admin;

import com.example.naebuilding.domain.Role;

import java.time.LocalDateTime;

public record AdminUserListItemDto(
        Long userId,
        String loginId,
        String nickname,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {}
