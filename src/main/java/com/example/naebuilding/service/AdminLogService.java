package com.example.naebuilding.service;

import com.example.naebuilding.domain.AdminLogEntity;
import com.example.naebuilding.dto.admin.AdminLogResponse;
import com.example.naebuilding.repository.AdminLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminLogRepository adminLogRepository;

    @Transactional
    public void write(AdminLogEntity log) {
        adminLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AdminLogResponse> search(
            LocalDateTime from,
            LocalDateTime to,
            String action,
            String actorLoginId,
            String keyword,
            Pageable pageable
    ) {
        Specification<AdminLogEntity> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (from != null) p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) p.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));

            if (action != null && !action.isBlank()) {
                p.add(cb.equal(root.get("action"), action.trim()));
            }

            if (actorLoginId != null && !actorLoginId.isBlank()) {
                p.add(cb.like(cb.lower(root.get("actorLoginId")), "%" + actorLoginId.trim().toLowerCase() + "%"));
            }

            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("message")), kw),
                        cb.like(cb.lower(root.get("targetType")), kw),
                        cb.like(cb.lower(root.get("action")), kw),
                        cb.like(cb.lower(root.get("actorLoginId")), kw)
                ));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };

        return adminLogRepository.findAll(spec, pageable)
                .map(this::toDto);
    }

    private AdminLogResponse toDto(AdminLogEntity e) {
        return new AdminLogResponse(
                e.getLogId(),
                e.getCreatedAt(),
                e.getActorUserId(),
                e.getActorLoginId(),
                e.getAction(),
                e.getTargetType(),
                e.getTargetId(),
                e.getMessage(),
                e.getIp()
        );
    }
}
