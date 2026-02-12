package com.example.naebuilding.dto.user;

public record UserMeResponse(
        Long userId,
        String loginId,
        String nickname,
        String email,
        String role
) {}
