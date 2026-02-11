package com.example.naebuilding.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("UNAUTHORIZED");
        }

        // JwtAuthFilter에서 principal을 userId(Long)로 넣었음
        if (auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        if (auth.getPrincipal() instanceof Integer intId) {
            return intId.longValue();
        }
        if (auth.getPrincipal() instanceof String s) {
            try { return Long.parseLong(s); } catch (Exception ignored) {}
        }

        throw new RuntimeException("UNAUTHORIZED");
    }
}
