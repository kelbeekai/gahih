package com.gahih.domain.member.service.email;

import com.gahih.domain.member.enumtype.EmailAuthPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MailEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendVerificationCode(String email, String code, EmailAuthPurpose purpose, LocalDateTime expiresAt) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject(emailTemplateService.createVerificationSubject(purpose));
        message.setText(emailTemplateService.createVerificationBody(purpose, code, expiresAt));
        javaMailSender.send(message);
    }

    @Override
    public void sendMaskedUsername(String email, String maskedUsername) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject(emailTemplateService.createMaskedUsernameSubject());
        message.setText(emailTemplateService.createMaskedUsernameBody(maskedUsername));
        javaMailSender.send(message);
    }

    @Override
    public void sendPasswordResetReadyNotice(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject(emailTemplateService.createPasswordResetReadySubject());
        message.setText(emailTemplateService.createPasswordResetReadyBody());
        javaMailSender.send(message);
    }
}