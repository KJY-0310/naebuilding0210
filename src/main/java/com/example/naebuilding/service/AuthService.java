package com.example.naebuilding.service;

import com.example.naebuilding.domain.Role;
import com.example.naebuilding.domain.UserEntity;
import com.example.naebuilding.dto.auth.SignupRequest;
import com.example.naebuilding.dto.auth.SignupResponse;
import com.example.naebuilding.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public SignupResponse signup(SignupRequest req) {

        // ✅ 이메일 인증 완료 필수 (서버 최종 검증)
        if (!emailVerificationService.isVerified(req.email())) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        if (userRepository.existsByLoginId(req.loginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(req.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        String encoded = passwordEncoder.encode(req.password());

        UserEntity saved = userRepository.save(
                new UserEntity(req.loginId(), encoded, req.nickname(), req.email(), Role.USER)
        );

        return new SignupResponse(saved.getUserId(), saved.getLoginId(), saved.getNickname());
    }
}
