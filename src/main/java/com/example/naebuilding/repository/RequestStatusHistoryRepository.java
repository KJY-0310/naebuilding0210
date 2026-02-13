package com.example.naebuilding.repository;

import com.example.naebuilding.domain.RequestStatusHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistoryEntity, Long> {

    @Query("""
        SELECT h
        FROM RequestStatusHistoryEntity h
        WHERE (:requestId IS NULL OR h.request.requestId = :requestId)
          AND (:actorLoginId IS NULL OR :actorLoginId = '' OR h.changedByLoginId LIKE CONCAT('%', :actorLoginId, '%'))
          AND (:from IS NULL OR h.createdAt >= :from)
          AND (:to IS NULL OR h.createdAt <= :to)
        ORDER BY h.historyId DESC
    """)
    Page<RequestStatusHistoryEntity> search(
            @Param("requestId") Long requestId,
            @Param("actorLoginId") String actorLoginId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
