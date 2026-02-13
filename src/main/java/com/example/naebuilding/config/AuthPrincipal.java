package com.example.naebuilding.config;

import java.security.Principal;

public record AuthPrincipal(Long userId, String loginId) implements Principal {
    @Override
    public String getName() {
        return loginId;
    }
}
