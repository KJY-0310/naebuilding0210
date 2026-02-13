package com.example.naebuilding.service;

import com.example.naebuilding.domain.NoticeEntity;
import com.example.naebuilding.dto.notice.NoticeListItemResponse;
import com.example.naebuilding.dto.notice.NoticeResponse;
import com.example.naebuilding.exception.NotFoundException;
import com.example.naebuilding.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeReadService {

    private final NoticeRepository noticeRepository;

    public List<NoticeListItemResponse> list() {
        return noticeRepository.findAllByOrderByNoticeIdDesc().stream()
                .map(this::toListItem)
                .toList();
    }

    public NoticeResponse detail(Long noticeId) {
        NoticeEntity e = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NotFoundException("공지를 찾을 수 없습니다. id=" + noticeId));
        return toDetail(e);
    }

    private NoticeListItemResponse toListItem(NoticeEntity e) {
        String body = e.getBody() == null ? "" : e.getBody();
        String preview = body.length() > 120 ? body.substring(0, 120) + "..." : body;

        return new NoticeListItemResponse(
                e.getNoticeId(),
                e.getTitle(),
                preview,
                e.getCreatedAt(),
                e.getCreatedByLoginId()
        );
    }

    private NoticeResponse toDetail(NoticeEntity e) {
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
