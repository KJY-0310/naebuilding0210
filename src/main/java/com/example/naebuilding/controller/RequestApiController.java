package com.example.naebuilding.controller;

import com.example.naebuilding.config.SecurityUtil;
import com.example.naebuilding.domain.RequestStatus;
import com.example.naebuilding.dto.*;
import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.dto.common.PageResponse;
import com.example.naebuilding.dto.request.RequestUpdateDto;
import com.example.naebuilding.repository.RequestRepository;
import com.example.naebuilding.service.FileStorageService;
import com.example.naebuilding.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Tag(name = "민원 API", description = "민원 CRUD 및 이미지 업로드/수정 기능")
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestApiController {

    private final FileStorageService fileStorageService;
    private final RequestService requestService;
    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper;

    // ✅ 상태별 카운트용
    private final RequestRepository requestRepository;

    @Operation(summary = "민원 상세 조회", description = "공개 API. 민원 상세와 이미지 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestDetailDto>> getDetail(@PathVariable Long id) {
        RequestDetailDto detail = requestService.getRequestDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    @Operation(summary = "민원 목록 조회", description = "공개 API. status/category/keyword + 페이징 조회를 지원합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RequestListItemDto>>> list(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RequestListItemDto> page = requestService.getRequestList(status, category, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    // ✅ (중요) 카테고리 목록 조회는 CategoryPublicController에서만 담당하도록 제거!
    // @GetMapping("/categories")  <-- 이거 때문에 CategoryPublicController와 충돌했음

    @Operation(summary = "상태별 민원 개수", description = "상태별 개수 및 TOTAL을 반환합니다.")
    @GetMapping("/status-counts")
    public ResponseEntity<ApiResponse<Map<String, Long>>> statusCounts() {
        List<Object[]> rows = requestRepository.countGroupByStatus();

        EnumMap<RequestStatus, Long> counts = new EnumMap<>(RequestStatus.class);
        for (RequestStatus s : RequestStatus.values()) counts.put(s, 0L);

        for (Object[] row : rows) {
            RequestStatus s = (RequestStatus) row[0];
            Long cnt = (Long) row[1];
            counts.put(s, cnt);
        }

        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        Map<String, Long> out = new LinkedHashMap<>();
        out.put("TOTAL", total);
        out.put("RECEIVED", counts.get(RequestStatus.RECEIVED));
        out.put("IN_PROGRESS", counts.get(RequestStatus.IN_PROGRESS));
        out.put("COMPLETED", counts.get(RequestStatus.COMPLETED));
        out.put("REJECTED", counts.get(RequestStatus.REJECTED));

        return ResponseEntity.ok(ApiResponse.ok(out));
    }

    @Operation(
            summary = "민원 등록",
            description = """
                    로그인 사용자만 가능.
                    RequestCreateRequest에서 writerId는 받지 않으며, 서버에서 로그인 유저로 writer를 세팅합니다.
                    multipart/form-data 로 data(JSON) + images(파일) 업로드를 지원합니다.
                    """
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> create(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {
        RequestCreateRequest data = objectMapper.readValue(dataJson, RequestCreateRequest.class);

        Long loginUserId = securityUtil.currentUserId();

        List<String> urls = new ArrayList<>();
        if (images != null) {
            for (MultipartFile f : images) urls.add(fileStorageService.save(f));
        }

        Long id = requestService.createRequest(loginUserId, data, urls);
        return ResponseEntity.ok(ApiResponse.ok("CREATED", id));
    }

    @Operation(
            summary = "민원 수정",
            description = """
                    로그인 사용자만 가능.
                    작성자 검증(validateOwner)을 통과해야 수정 가능.
                    multipart/form-data 로 data(JSON) + deleteImageIds(JSON string) + images(파일) 업로드를 지원합니다.
                    """
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RequestDetailDto>> updateRequest(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "deleteImageIds", required = false) String deleteImageIdsJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {
        RequestUpdateDto data = objectMapper.readValue(dataJson, RequestUpdateDto.class);

        Long loginUserId = securityUtil.currentUserId();
        RequestDetailDto updated = requestService.updateRequest(id, data, deleteImageIdsJson, images, loginUserId);
        return ResponseEntity.ok(ApiResponse.ok("UPDATED", updated));
    }
}
