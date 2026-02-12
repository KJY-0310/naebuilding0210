package com.example.naebuilding.service;

import com.example.naebuilding.domain.EmailVerificationEntity;
import com.example.naebuilding.repository.EmailVerificationRepository;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.mail.from-name:Naebuilding}")
    private String mailFromName;

    private final SecureRandom random = new SecureRandom();

    private static final String PURPOSE_SIGNUP = "SIGNUP";
    private static final int CODE_LEN = 6;

    // ✅ 인증코드 유효시간: 5분
    private static final long EXPIRE_MINUTES = 5;

    // ✅ 인증코드 재발송 쿨타임: 30초
    private static final long RESEND_COOLDOWN_SECONDS = 30;

    /**
     * ✅ 인증코드 발송(재발송 포함)
     * - DB(email unique) 구조에 맞게 upsert(있으면 update, 없으면 insert)
     * - 쿨타임(30초) 강제
     * - 실제 SMTP 발송(MimeMessage)
     */
    @Transactional
    public void sendCode(String email) {
        EmailVerificationEntity rec = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, PURPOSE_SIGNUP)
                .orElse(null);

        // ✅ 재발송 쿨타임 체크(createdAt 기준)
        if (rec != null && rec.getCreatedAt() != null) {
            LocalDateTime cooldownEnd = rec.getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS);
            if (LocalDateTime.now().isBefore(cooldownEnd)) {
                long left = Duration.between(LocalDateTime.now(), cooldownEnd).getSeconds();
                throw new IllegalArgumentException("인증코드 재발송은 " + left + "초 후에 가능합니다.");
            }
        }

        // ✅ 코드 생성
        String code = String.format("%0" + CODE_LEN + "d", random.nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);

        // ✅ upsert(중복키 에러 방지)
        if (rec == null) {
            rec = new EmailVerificationEntity(email, code, PURPOSE_SIGNUP, expiresAt, false);
        } else {
            rec.updateCode(code, expiresAt);
        }
        emailVerificationRepository.save(rec);

        // ✅ 실제 메일 발송
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(email);
            helper.setFrom(new InternetAddress(mailFrom, mailFromName));
            helper.setSubject("[naebuilding] 이메일 인증코드");
            helper.setText(
                    "인증코드: " + code + "\n" +
                            "유효시간: " + EXPIRE_MINUTES + "분\n\n" +
                            "감사합니다.",
                    false
            );

            mailSender.send(message);
            log.info("[EMAIL_SENT] email={}, code={}, expiresAt={}", email, code, expiresAt);

        } catch (Exception e) {
            log.error("[EMAIL_SEND_FAIL] email={}", email, e);
            throw new IllegalArgumentException("메일 발송에 실패했습니다. SMTP 설정을 확인해주세요.");
        }
    }

    /**
     * ✅ 인증코드 검증
     */
    @Transactional
    public void verify(String email, String code) {
        EmailVerificationEntity rec = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, PURPOSE_SIGNUP)
                .orElseThrow(() -> new IllegalArgumentException("인증코드를 먼저 발송해주세요."));

        if (LocalDateTime.now().isAfter(rec.getExpiresAt())) {
            throw new IllegalArgumentException("인증코드가 만료되었습니다. 다시 발송해주세요.");
        }
        if (!rec.getCode().equals(code)) {
            throw new IllegalArgumentException("인증코드가 올바르지 않습니다.");
        }

        rec.markVerified();
        emailVerificationRepository.save(rec);
    }

    /**
     * ✅ 인증 완료 여부(회원가입 시 서버에서 최종 검증용)
     */
    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        EmailVerificationEntity rec = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, PURPOSE_SIGNUP)
                .orElse(null);

        if (rec == null) return false;
        if (LocalDateTime.now().isAfter(rec.getExpiresAt())) return false;
        return rec.isVerified();
    }
}
