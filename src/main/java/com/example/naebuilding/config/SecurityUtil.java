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

        // ✅ JwtAuthFilter에서 principal=userId(Long)로 넣는 방식 지원
        if (principal instanceof Long) {
            return (Long) principal;
        }

        // ✅ 혹시 principal이 String으로 들어오는 경우 대비
        if (principal instanceof String s) {
            if ("anonymousUser".equals(s)) throw new AccessDeniedException("익명 사용자입니다.");
            return Long.valueOf(s);
        }

        // ✅ 나중에 CustomUserDetails를 principal로 넣게 되더라도 안전
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }

        throw new AccessDeniedException("지원하지 않는 principal 타입: " + principal.getClass());
    }
}
