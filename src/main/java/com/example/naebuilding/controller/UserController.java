package com.example.naebuilding.controller;

import com.example.naebuilding.config.SecurityUtil;
import com.example.naebuilding.domain.UserEntity;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.dto.user.UserMeResponse;
import com.example.naebuilding.exception.NotFoundException;
import com.example.naebuilding.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> me() {
        Long userId = securityUtil.currentUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        UserMeResponse body = new UserMeResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getNickname(),
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(ApiResponse.ok(body));
    }
}
