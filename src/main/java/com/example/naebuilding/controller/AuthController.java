package com.example.naebuilding.controller;

import com.example.naebuilding.config.JwtProvider;
import com.example.naebuilding.domain.UserEntity;
import com.example.naebuilding.dto.auth.*;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.exception.NotFoundException;
import com.example.naebuilding.repository.UserRepository;
import com.example.naebuilding.service.AuthService;   // ✅ 추가
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.naebuilding.dto.auth.EmailSendRequest;
import com.example.naebuilding.dto.auth.EmailVerifyRequest;
import com.example.naebuilding.service.EmailVerificationService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest req, HttpServletResponse res) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.loginId(), req.password())
        );

        UserEntity user = userRepository.findByLoginId(req.loginId())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        String access = jwtProvider.createAccessToken(user.getUserId(), user.getLoginId(), user.getRole().name());
        String refresh = jwtProvider.createRefreshToken(user.getUserId());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refresh)
                .httpOnly(true)
                .secure(false)
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

    // ✅ 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@RequestBody @Valid SignupRequest req) {
        SignupResponse out = authService.signup(req);
        return ResponseEntity.ok(ApiResponse.ok("SIGNED_UP", out));
    }

    //아이디 중복 체크
    @GetMapping("/check-loginId")
    public ResponseEntity<ApiResponse<Void>> checkLoginId(@RequestParam String loginId) {
        boolean exists = userRepository.existsByLoginId(loginId);
        if (exists) throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        return ResponseEntity.ok(ApiResponse.ok("OK", null));
    }

    // 이메일 인증코드 발송
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendEmailCode(@RequestBody @Valid EmailSendRequest req) {
        emailVerificationService.sendCode(req.email());
        return ResponseEntity.ok(ApiResponse.ok("SENT", null));
    }

    // 이메일 인증코드 검증
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmailCode(@RequestBody @Valid EmailVerifyRequest req) {
        emailVerificationService.verify(req.email(), req.code());
        return ResponseEntity.ok(ApiResponse.ok("VERIFIED", null));
    }


}
