package com.example.naebuilding.service;

import com.example.naebuilding.domain.AdminLogEntity;
import com.example.naebuilding.domain.NoticeEntity;
import com.example.naebuilding.dto.admin.NoticeCreateRequest;
import com.example.naebuilding.dto.admin.NoticeResponse;
import com.example.naebuilding.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final AdminLogService adminLogService;

    @Transactional(readOnly = true)
    public List<NoticeResponse> list() {
        return noticeRepository.findAll().stream()
                .sorted(Comparator.comparingLong(NoticeEntity::getNoticeId).reversed())
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public NoticeResponse create(
            NoticeCreateRequest req,
            Long actorUserId,
            String actorLoginId,
            String ip,
            String userAgent
    ) {
        String title = req.title() == null ? "" : req.title().trim();
        String body = req.body() == null ? "" : req.body().trim();

        NoticeEntity saved = noticeRepository.save(
                new NoticeEntity(title, body, actorUserId, actorLoginId)
        );

        adminLogService.write(new AdminLogEntity(
                actorUserId,
                actorLoginId,
                "NOTICE_CREATE",
                "NOTICE",
                saved.getNoticeId(),
                "공지 등록: " + title,
                ip,
                userAgent
        ));

        return toDto(saved);
    }

    private NoticeResponse toDto(NoticeEntity e) {
        return new NoticeResponse(
                e.getNoticeId(),
                e.getTitle(),
                e.getBody(),
                e.getCreatedAt(),
                e.getCreatedBy(),
                e.getCreatedByLoginId()
        );
    }
}
