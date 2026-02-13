package com.example.naebuilding.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Column(nullable = false, length = 120)
    private String title;

    // ✅ DB가 TEXT면 엔티티도 TEXT로 고정 (스키마 검증 충돌 방지)
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private Long createdBy;

    @Column(nullable = true, length = 60)
    private String createdByLoginId;

    public NoticeEntity(String title, String body, Long createdBy, String createdByLoginId) {
        this.title = title;
        this.body = body;
        this.createdBy = createdBy;
        this.createdByLoginId = createdByLoginId;
    }
}
