package com.example.naebuilding.service;

import com.example.naebuilding.domain.RequestEntity;
import com.example.naebuilding.domain.RequestStatusHistoryEntity;
import com.example.naebuilding.dto.RequestStatusHistoryResponse;
import com.example.naebuilding.dto.common.PageResponse;
import com.example.naebuilding.repository.RequestStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestStatusHistoryService {

    private final RequestStatusHistoryRepository historyRepository;

    @Transactional
    public void record(
            RequestEntity request,
            String beforeStatus,
            String afterStatus,
            Long actorUserId,
            String actorLoginId,
            String ip,
            String userAgent
    ) {
        RequestStatusHistoryEntity h = new RequestStatusHistoryEntity(
                request,
                beforeStatus,
                afterStatus,
                actorUserId,
                actorLoginId,
                ip,
                userAgent
        );
        historyRepository.save(h);
    }

    public PageResponse<RequestStatusHistoryResponse> search(
            Long requestId,
            String actorLoginId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        Page<RequestStatusHistoryEntity> page = historyRepository.search(requestId, actorLoginId, from, to, pageable);

        Page<RequestStatusHistoryResponse> mapped = page.map(h -> new RequestStatusHistoryResponse(
                h.getHistoryId(),
                h.getRequest().getRequestId(),
                h.getBeforeStatus(),
                h.getAfterStatus(),
                h.getChangedByUserId(),
                h.getChangedByLoginId(),
                h.getIp(),
                h.getUserAgent(),
                h.getCreatedAt()
        ));

        return PageResponse.from(mapped);
    }
}
