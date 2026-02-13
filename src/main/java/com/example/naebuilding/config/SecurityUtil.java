package com.example.naebuilding.config;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof AuthPrincipal ap) return ap.userId();
        if (principal instanceof Long l) return l;

        if (principal instanceof String s) {
            if ("anonymousUser".equals(s)) throw new AccessDeniedException("익명 사용자입니다.");
            return Long.valueOf(s);
        }

        if (principal instanceof CustomUserDetails cud) return cud.getUserId();

        throw new AccessDeniedException("지원하지 않는 principal 타입: " + principal.getClass());
    }

    public String currentLoginId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof AuthPrincipal ap) return ap.loginId();

        return auth.getName();
    }
}
