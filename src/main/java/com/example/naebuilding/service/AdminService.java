package com.example.naebuilding.service;

import com.example.naebuilding.domain.Role;
import com.example.naebuilding.domain.UserEntity;
import com.example.naebuilding.dto.admin.*;
import com.example.naebuilding.exception.NotFoundException;
import com.example.naebuilding.repository.RequestRepository;
import com.example.naebuilding.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    // =========================
    // 사용자 관리
    // =========================

    @Transactional(readOnly = true)
    public List<AdminUserListItemDto> listUsers() {
        return userRepository.findAdminUserList(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional
    public void updateUserRole(Long userId, Role role) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        // ✅ 자기 자신 role 바꾸는 걸 막고 싶으면(선택):
        // Long me = securityUtil.currentUserId(); 이런 식으로 받아서 막을 수 있음 (프론트 붙이고 나서 추가하자)

        // role 변경
        // (UserEntity에 setter가 없으니, 편의 메서드 추가하거나 Reflection 쓰지 말고 메서드 추가 추천)
        // 여기서는 "메서드 추가" 방식으로 갈게.
        user.changeRole(role);
    }

    @Transactional
    public void updateUserActive(Long userId, boolean active) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        user.changeActive(active);
    }

    // =========================
    // 통계
    // =========================

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {

        // 월별
        List<Object[]> monthlyRows = requestRepository.countMonthly();
        List<MonthlyCountDto> monthly = monthlyRows.stream()
                .map(r -> new MonthlyCountDto(
                        String.valueOf(r[0]),
                        ((Number) r[1]).longValue()
                ))
                .toList();

        // 상태별
        List<Object[]> statusRows = requestRepository.countGroupByStatus();
        Map<String, Long> status = new LinkedHashMap<>();
        long total = 0L;

        for (Object[] row : statusRows) {
            String key = String.valueOf(row[0]); // RECEIVED...
            long cnt = ((Number) row[1]).longValue();
            status.put(key, cnt);
            total += cnt;
        }

        long completed = status.getOrDefault("COMPLETED", 0L);
        long rejected = status.getOrDefault("REJECTED", 0L);

        int completionRate = (total == 0) ? 0 : (int) Math.round((completed * 100.0) / total);
        int rejectRate = (total == 0) ? 0 : (int) Math.round((rejected * 100.0) / total);

        return new AdminStatsResponse(monthly, status, completionRate, rejectRate);
    }
}
