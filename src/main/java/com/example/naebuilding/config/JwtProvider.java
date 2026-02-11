package com.example.naebuilding.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtProvider {

    private final Key key;
    private final long accessMin;
    private final long refreshDays;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-exp-min}") long accessMin,
            @Value("${jwt.refresh-token-exp-days}") long refreshDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessMin = accessMin;
        this.refreshDays = refreshDays;
    }

    public String createAccessToken(Long userId, String loginId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("loginId", loginId)
                .claim("role", role) // ✅ role 저장
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessMin, ChronoUnit.MINUTES)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(refreshDays, ChronoUnit.DAYS)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getBody().getSubject());
    }

    // ✅ 추가: accessToken에서 role 꺼내기
    public String getRole(String token) {
        return parse(token).getBody().get("role", String.class);
    }

    // ✅ 추가: accessToken에서 loginId 꺼내기(선택: 필요할 때)
    public String getLoginId(String token) {
        return parse(token).getBody().get("loginId", String.class);
    }
}
