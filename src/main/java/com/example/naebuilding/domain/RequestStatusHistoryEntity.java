package com.example.naebuilding.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "request_status_history",
        indexes = {
                @Index(name = "idx_rsh_request_id", columnList = "request_id"),
                @Index(name = "idx_rsh_created_at", columnList = "created_at"),
                @Index(name = "idx_rsh_changed_by", columnList = "changed_by_user_id")
        }
)
@Getter
@NoArgsConstructor
public class RequestStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestEntity request;

    @Column(name = "before_status", nullable = false, length = 20)
    private String beforeStatus;

    @Column(name = "after_status", nullable = false, length = 20)
    private String afterStatus;

    @Column(name = "changed_by_user_id", nullable = false)
    private Long changedByUserId;

    @Column(name = "changed_by_login_id", length = 50)
    private String changedByLoginId;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RequestStatusHistoryEntity(
            RequestEntity request,
            String beforeStatus,
            String afterStatus,
            Long changedByUserId,
            String changedByLoginId,
            String ip,
            String userAgent
    ) {
        this.request = request;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.changedByUserId = changedByUserId;
        this.changedByLoginId = changedByLoginId;
        this.ip = ip;
        this.userAgent = userAgent;
    }
}
