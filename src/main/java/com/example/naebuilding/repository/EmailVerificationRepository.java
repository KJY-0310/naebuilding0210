package com.example.naebuilding.repository;

import com.example.naebuilding.domain.EmailVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, Long> {
    Optional<EmailVerificationEntity> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, String purpose);
}
