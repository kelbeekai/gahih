package com.gahih.domain.report.enumtype;

public enum ReportReasonType {

    SPAM("스팸/광고"),
    ABUSE_OR_HARASSMENT("욕설/괴롭힘"),
    HATE_OR_DISCRIMINATION("혐오/차별"),
    SEXUAL_OR_OBSCENE("음란/선정성"),
    VIOLENCE_OR_ILLEGAL("폭력/불법"),
    IMPERSONATION_OR_FRAUD("사칭/사기"),
    PRIVACY_EXPOSURE("개인정보 노출"),
    INAPPROPRIATE_NICKNAME("부적절한 닉네임"),
    INAPPROPRIATE_ATTACHMENT("부적절한 첨부파일"),
    OTHER("기타");

    private final String description;

    ReportReasonType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}