package com.example.naebuilding.repository;

import com.example.naebuilding.domain.RequestEntity;
import com.example.naebuilding.domain.RequestStatus;
import com.example.naebuilding.dto.RequestListItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

    @Query("""
        SELECT new com.example.naebuilding.dto.RequestListItemDto(
            r.requestId,
            r.title,
            r.category,
            r.location,
            r.status,
            w.nickname,
            r.createdAt
        )
        FROM RequestEntity r
        JOIN r.writer w
        WHERE (:status IS NULL OR r.status = :status)
          AND (:category IS NULL OR r.category = :category)
          AND (
              :keyword IS NULL OR :keyword = '' OR
              r.title LIKE %:keyword% OR
              r.content LIKE %:keyword% OR
              w.nickname LIKE %:keyword%
          )
    """)
    Page<RequestListItemDto> findList(
            @Param("status") RequestStatus status,
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT r
        FROM RequestEntity r
        JOIN FETCH r.writer w
        WHERE r.requestId = :requestId
    """)
    java.util.Optional<RequestEntity> findByIdWithWriter(@Param("requestId") Long requestId);

    @Query("""
        SELECT DISTINCT r.category
        FROM RequestEntity r
        ORDER BY r.category ASC
    """)
    List<String> findDistinctCategories();

    // ✅ 상태별 개수 집계
    @Query("select r.status as status, count(r) as cnt from RequestEntity r group by r.status")
    List<Object[]> countGroupByStatus();

    // ✅ 월별 민원 수 (MariaDB)
    @Query(value = """
        SELECT DATE_FORMAT(created_at, '%Y-%m') AS ym, COUNT(*) AS cnt
        FROM requests
        GROUP BY ym
        ORDER BY ym
    """, nativeQuery = true)
    List<Object[]> countMonthly();
}
