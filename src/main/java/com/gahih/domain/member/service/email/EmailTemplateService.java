package com.gahih.domain.member.service.email;

import com.gahih.domain.member.enumtype.EmailAuthPurpose;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EmailTemplateService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String createVerificationSubject(EmailAuthPurpose purpose) {
        return switch (purpose) {
            case SIGNUP_VERIFY -> "[가힣] 회원가입 이메일 인증코드";
            case USERNAME_RECOVERY -> "[가힣] 아이디 찾기 인증코드";
            case PASSWORD_RESET_VERIFY -> "[가힣] 비밀번호 재설정 인증코드";
            case EMAIL_CHANGE_VERIFY -> "[가힣] 이메일 변경 인증코드";
        };
    }

    public String createVerificationBody(EmailAuthPurpose purpose, String code, LocalDateTime expiresAt) {
        String purposeText = switch (purpose) {
            case SIGNUP_VERIFY -> "회원가입";
            case USERNAME_RECOVERY -> "아이디 찾기";
            case PASSWORD_RESET_VERIFY -> "비밀번호 재설정";
            case EMAIL_CHANGE_VERIFY -> "이메일 변경";
        };

        return """
                안녕하세요. 가힣입니다.

                아래 인증코드를 입력해 %s 절차를 완료해주세요.

                인증코드: %s
                만료 시각: %s

                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """
                .formatted(
                        purposeText,
                        code,
                        expiresAt.format(DATE_TIME_FORMATTER)
                );
    }

    public String createMaskedUsernameSubject() {
        return "[가힣] 아이디 찾기 결과 안내";
    }

    public String createMaskedUsernameBody(String maskedUsername) {
        return """
                안녕하세요. 가힣입니다.

                요청하신 계정의 아이디는 아래와 같습니다.

                아이디: %s

                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """
                .formatted(maskedUsername);
    }

    public String createPasswordResetReadySubject() {
        return "[가힣] 비밀번호 재설정 인증 완료 안내";
    }

    public String createPasswordResetReadyBody() {
        return """
                안녕하세요. 가힣입니다.

                비밀번호 재설정 인증이 완료되었습니다.
                이제 비밀번호 재설정 화면에서 새 비밀번호를 설정할 수 있습니다.

                본인이 요청하지 않았다면 즉시 관리자에게 문의해주세요.
                """;
    }
}