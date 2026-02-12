package com.example.naebuilding.dto.auth;

public record SignupResponse(
        Long userId,
        String loginId,
        String nickname
) {}
