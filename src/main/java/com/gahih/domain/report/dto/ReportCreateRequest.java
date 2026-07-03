package com.gahih.domain.report.dto;

import com.gahih.domain.report.enumtype.ReportReasonType;
import com.gahih.domain.report.enumtype.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportCreateRequest {

    @NotNull(message = "신고 대상 타입은 필수입니다.")
    private ReportTargetType targetType;

    @NotNull(message = "신고 대상 ID는 필수입니다.")
    private Long targetId;

    @NotNull(message = "신고 사유는 필수입니다.")
    private ReportReasonType reasonType;

    @Size(max = 1000, message = "신고 상세 내용은 1000자를 초과할 수 없습니다.")
    private String detail;
}