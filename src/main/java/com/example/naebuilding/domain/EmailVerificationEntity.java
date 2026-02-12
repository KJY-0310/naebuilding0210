package com.example.naebuilding.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "email_verifications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_email_verifications_email", columnNames = "email")
        }
)
@Getter
@NoArgsConstructor
public class EmailVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "purpose", nullable = false, length = 20)
    private String purpose; // 예: SIGNUP

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public EmailVerificationEntity(String email, String code, String purpose, LocalDateTime expiresAt, boolean verified) {
        this.email = email;
        this.code = code;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
        this.verified = verified;
    }

    public void updateCode(String code, LocalDateTime expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = false;
    }

    public void markVerified() {
        this.verified = true;
    }
}
