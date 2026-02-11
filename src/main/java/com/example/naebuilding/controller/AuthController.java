package com.example.naebuilding.controller;

import com.example.naebuilding.config.JwtProvider;
import com.example.naebuilding.domain.UserEntity;
import com.example.naebuilding.dto.auth.LoginRequest;
import com.example.naebuilding.dto.auth.LoginResponse;
import com.example.naebuilding.dto.auth.RefreshResponse;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.exception.NotFoundException;
import com.example.naebuilding.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest req, HttpServletResponse res) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.loginId(), req.password())
        );

        UserEntity user = userRepository.findByLoginId(req.loginId())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        String access = jwtProvider.createAccessToken(user.getUserId(), user.getLoginId(), user.getRole().name());
        String refresh = jwtProvider.createRefreshToken(user.getUserId());

        // refreshToken을 HttpOnly 쿠키로 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refresh)
                .httpOnly(true)
                .secure(false) // https면 true
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        res.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(ApiResponse.ok(new LoginResponse(access)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.fail("NO_REFRESH_TOKEN", null));
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        String newAccess = jwtProvider.createAccessToken(user.getUserId(), user.getLoginId(), user.getRole().name());
        return ResponseEntity.ok(ApiResponse.ok(new RefreshResponse(newAccess)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        res.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(ApiResponse.ok("LOGOUT", null));
    }
}
