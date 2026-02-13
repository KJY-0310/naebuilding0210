package com.example.naebuilding.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "admin_logs",
        indexes = {
                @Index(name = "idx_admin_logs_created_at", columnList = "created_at"),
                @Index(name = "idx_admin_logs_actor", columnList = "actor_user_id, created_at"),
                @Index(name = "idx_admin_logs_action", columnList = "action, created_at")
        }
)
@Getter
@NoArgsConstructor
public class AdminLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(name = "actor_login_id", length = 50)
    private String actorLoginId;

    @Column(name = "action", nullable = false, length = 60)
    private String action;

    @Column(name = "target_type", length = 60)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    public AdminLogEntity(
            Long actorUserId,
            String actorLoginId,
            String action,
            String targetType,
            Long targetId,
            String message,
            String ip,
            String userAgent
    ) {
        this.actorUserId = actorUserId;
        this.actorLoginId = actorLoginId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.message = message;
        this.ip = ip;
        this.userAgent = userAgent;
    }
}
