package com.example.naebuilding.domain;

import com.example.naebuilding.service.FileStorageService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "requests",
        indexes = {
                @Index(name = "idx_requests_writer_created", columnList = "writer_id, created_at"),
                @Index(name = "idx_requests_status", columnList = "status"),
                @Index(name = "idx_requests_category", columnList = "category")
        }
)
@Getter
@NoArgsConstructor
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    // writer_id FK → users.user_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "writer_id", nullable = false)
    private UserEntity writer;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ✅ 이미지 N개 (정렬: sort_order ASC)
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<RequestImageEntity> images = new ArrayList<>();

    // ✅ 생성 편의
    public RequestEntity(UserEntity writer, String title, String category, String location, String content) {
        this.writer = writer;
        this.title = title;
        this.category = category;
        this.location = location;
        this.content = content;
        this.status = RequestStatus.RECEIVED; // 기본 접수
    }

    // ✅ enum으로 받기
    public void changeStatus(RequestStatus status) {
        this.status = status;
    }

    // ✅ 서비스에서 호출명 맞추기
    public void changeAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public void addImage(RequestImageEntity image) {
        image.setRequest(this);
        this.images.add(image);
    }

    public void update(String title, String content, String category, String location) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.location = location;
    }

    public void addImage(String imageUrl, int sortOrder) {
        RequestImageEntity img = new RequestImageEntity(imageUrl, sortOrder);
        addImage(img); // ✅ 기존 addImage(entity)를 재사용
    }

    public int nextSortOrder() {
        return this.images.stream()
                .map(RequestImageEntity::getSortOrder)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    public List<String> removeImagesByIds(List<Long> deleteIds) {
        List<String> removedUrls = new ArrayList<>();

        this.images.removeIf(img -> {
            if (deleteIds.contains(img.getImageId())) {
                removedUrls.add(img.getImageUrl());
                return true;
            }
            return false;
        });

        for (int i = 0; i < this.images.size(); i++) {
            this.images.get(i).changeSortOrder(i + 1);
        }
        return removedUrls;
    }






}
