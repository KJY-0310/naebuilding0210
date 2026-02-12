package com.example.naebuilding.repository;

import com.example.naebuilding.domain.UserEntity;
import com.example.naebuilding.dto.admin.AdminUserListItemDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    // ✅ 관리자 사용자 목록 (password 제외)
    @Query("""
        select new com.example.naebuilding.dto.admin.AdminUserListItemDto(
            u.userId,
            u.loginId,
            u.nickname,
            u.email,
            u.role,
            u.active,
            u.createdAt
        )
        from UserEntity u
    """)
    List<AdminUserListItemDto> findAdminUserList(Sort sort);
}
