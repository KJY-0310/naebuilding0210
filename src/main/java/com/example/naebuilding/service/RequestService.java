package com.example.naebuilding.service;

import com.example.naebuilding.config.SecurityUtil;
import com.example.naebuilding.domain.*;
import com.example.naebuilding.dto.*;
import com.example.naebuilding.dto.request.RequestUpdateDto;
import com.example.naebuilding.exception.NotFoundException;
import com.example.naebuilding.repository.RequestImageRepository;
import com.example.naebuilding.repository.RequestRepository;
import com.example.naebuilding.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestImageRepository requestImageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    // ✅ 상태 변경 이력
    private final RequestStatusHistoryService requestStatusHistoryService;
    private final SecurityUtil securityUtil;

    // ✅ 민원 상세
    public RequestDetailDto getRequestDetail(Long requestId) {

        RequestEntity r = requestRepository.findByIdWithWriter(requestId)
                .orElseThrow(() -> new NotFoundException("민원을 찾을 수 없습니다. id=" + requestId));

        List<RequestImageDto> images = requestImageRepository.findImageDtosByRequestId(requestId);

        return new RequestDetailDto(
                r.getRequestId(),
                r.getTitle(),
                r.getContent(),
                r.getCategory(),
                r.getLocation(),
                String.valueOf(r.getStatus()),
                r.getWriter().getNickname(),
                r.getAdminNote(),
                r.getCreatedAt(),
                images
        );
    }

    // ✅ 상태 변경 + 이력 저장
    @Transactional
    public void updateStatus(Long requestId, RequestStatus nextStatus, HttpServletRequest httpReq) {
        RequestEntity r = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("민원을 찾을 수 없습니다. id=" + requestId));

        RequestStatus beforeStatus = r.getStatus();

        // 상태가 같으면 아무것도 안함
        if (beforeStatus == nextStatus) return;

        // 1) 상태 변경
        r.changeStatus(nextStatus);

        // 2) actor
        Long actorUserId = securityUtil.currentUserId();
        String actorLoginId = securityUtil.currentLoginId();

        // 3) ip/ua
        String ip = extractClientIp(httpReq);
        String ua = httpReq != null ? httpReq.getHeader("User-Agent") : null;

        // 4) 기록 저장 (문자열은 enum.name()으로 고정)
        requestStatusHistoryService.record(
                r,
                beforeStatus.name(),
                nextStatus.name(),
                actorUserId,
                actorLoginId,
                ip,
                ua
        );
    }

    // 프록시/리버스프록시 대비(없으면 remoteAddr)
    private String extractClientIp(HttpServletRequest req) {
        if (req == null) return null;

        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }

        String xrip = req.getHeader("X-Real-IP");
        if (xrip != null && !xrip.isBlank()) return xrip.trim();

        return req.getRemoteAddr();
    }

    // ✅ 관리자 메모 수정
    @Transactional
    public void updateAdminNote(Long requestId, String adminNote) {
        RequestEntity r = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("민원을 찾을 수 없습니다. id=" + requestId));

        r.changeAdminNote(adminNote);
    }

    // ✅ 목록 조회
    public Page<RequestListItemDto> getRequestList(
            RequestStatus status,
            String category,
            String keyword,
            Pageable pageable
    ) {
        return requestRepository.findList(status, category, keyword, pageable);
    }

    // ✅ 카테고리 목록
    public List<String> getCategories() {
        return requestRepository.findDistinctCategories();
    }

    // ✅ 민원 등록
    @Transactional
    public Long createRequest(Long loginUserId, RequestCreateRequest req, List<String> imageUrls) {

        UserEntity writer = userRepository.findById(loginUserId)
                .orElseThrow(() -> new NotFoundException("작성자를 찾을 수 없습니다. id=" + loginUserId));

        RequestEntity entity = new RequestEntity(
                writer,
                req.title(),
                req.category(),
                req.location(),
                req.content()
        );

        for (int i = 0; i < imageUrls.size(); i++) {
            RequestImageEntity img = new RequestImageEntity(imageUrls.get(i), i + 1);
            entity.addImage(img);
        }

        return requestRepository.save(entity).getRequestId();
    }

    @Transactional
    public RequestDetailDto updateRequest(
            Long requestId,
            RequestUpdateDto dto,
            String deleteImageIdsJson,
            List<MultipartFile> newImages,
            Long loginUserId
    ) {
        RequestEntity request = requestRepository.findByIdWithWriter(requestId)
                .orElseThrow(() -> new NotFoundException("민원을 찾을 수 없습니다. id=" + requestId));

        validateOwner(request, loginUserId);

        request.update(dto.title(), dto.content(), dto.category(), dto.location());

        // 2) 삭제
        List<Long> deleteIds = parseDeleteIds(deleteImageIdsJson);
        if (!deleteIds.isEmpty()) {
            List<String> removedUrls = request.removeImagesByIds(deleteIds);

            if (removedUrls.isEmpty()) {
                throw new IllegalArgumentException("요청한 deleteImageIds가 현재 민원에 존재하지 않습니다.");
            }

            for (String url : removedUrls) {
                fileStorageService.deleteByUrl(url);
            }
        }

        // 3) 추가
        if (newImages != null && !newImages.isEmpty()) {
            int nextSort = request.nextSortOrder();
            for (MultipartFile f : newImages) {
                if (f == null || f.isEmpty()) continue;
                String url = fileStorageService.save(f);
                request.addImage(url, nextSort++);
            }
        }

        return getRequestDetail(requestId);
    }

    private void validateOwner(RequestEntity request, Long loginUserId) {
        Long writerId = request.getWriter().getUserId();
        if (!writerId.equals(loginUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("작성자만 수정/삭제할 수 있습니다.");
        }
    }

    private List<Long> parseDeleteIds(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("deleteImageIds 형식이 올바르지 않습니다. 예: [1,2]");
        }
    }
}
