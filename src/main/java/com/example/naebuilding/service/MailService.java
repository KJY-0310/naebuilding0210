package com.example.naebuilding.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name:Naebuilding}")
    private String fromName;

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromName + " <" + from + ">");
        msg.setTo(to);
        msg.setSubject("[Naebuilding] 이메일 인증코드");
        msg.setText("인증코드: " + code + "\n\n유효시간은 5분입니다.\n만약 본인이 요청하지 않았다면 이 메일을 무시하세요.");
        mailSender.send(msg);
    }
}
