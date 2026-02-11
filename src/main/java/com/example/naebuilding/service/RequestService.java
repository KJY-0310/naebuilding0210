package com.example.naebuilding.service;

import com.example.naebuilding.domain.*;
import com.example.naebuilding.dto.*;
import com.example.naebuilding.dto.request.RequestUpdateDto;
import com.example.naebuilding.exception.NotFoundException;
import com.example.naebuilding.repository.RequestImageRepository;
import com.example.naebuilding.repository.RequestRepository;
import com.example.naebuilding.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    // ✅ 상태 변경
    @Transactional
    public void updateStatus(Long requestId, RequestStatus status) {
        RequestEntity r = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("민원을 찾을 수 없습니다. id=" + requestId));

        r.changeStatus(status);
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

    // ✅ 민원 등록 + 이미지 저장 (multipart 업로드 결과 URL 저장)
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
            Long loginUserId // ✅ 추가
    ) {
        RequestEntity request = requestRepository.findByIdWithWriter(requestId) // ✅ writer까지 fetch 추천
                .orElseThrow(() -> new NotFoundException("민원을 찾을 수 없습니다. id=" + requestId));

        validateOwner(request, loginUserId); // ✅ 핵심 추가

        request.update(dto.title(), dto.content(), dto.category(), dto.location());

        // 2) 삭제
        List<Long> deleteIds = parseDeleteIds(deleteImageIdsJson);
        if (!deleteIds.isEmpty()) {
            List<String> removedUrls = request.removeImagesByIds(deleteIds);

            if (removedUrls.isEmpty()) {
                throw new IllegalArgumentException("요청한 deleteImageIds가 현재 민원에 존재하지 않습니다.");
            }

            // ✅ 파일 삭제는 서비스가 담당
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

        // 4) 최신 상세 반환 (이미 너가 만든 방식 재사용)
        return getRequestDetail(requestId);
    }

    private void validateOwner(RequestEntity request, Long loginUserId) {
        Long writerId = request.getWriter().getUserId(); // 너희 User PK명에 맞춰 유지
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
