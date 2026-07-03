package com.gahih.domain.category.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryCode {

    NOTICE(
            "공지사항",
            "공지사항, 서비스 이용안내 등 커뮤니티 전반의 운영 정보를 안내하는 게시판",
            1,
            true,
            true,
            false,
            false,
            false
    ),
    INQUIRY(
            "이용문의",
            "FAQ, 운영 문의, 신고, 건의사항 등 운영자와 회원이 소통하는 게시판",
            2,
            true,
            false,
            true,
            false,
            true
    ),
    STUDY_IMMIGRATION(
            "유학·이민",
            "유학, 비자, 이민, 정착과 관련된 질문과 정보를 나누는 게시판",
            3,
            true,
            false,
            true,
            true,
            false
    ),
    LOCAL_LIFE(
            "지역생활",
            "지역별 생활 정보, 친목, 일상 팁을 나누는 게시판",
            4,
            true,
            false,
            true,
            true,
            false
    ),
    JOB(
            "구인구직",
            "채용 공고, 구직, 아르바이트, 단기 일자리 정보를 공유하는 게시판",
            5,
            true,
            false,
            false,
            false,
            false
    ),
    TRAVEL(
            "여행정보",
            "여행 준비, 일정, 교통, 숙소, 현지 팁을 나누는 게시판",
            6,
            true,
            false,
            true,
            true,
            false
    ),
    HOUSING(
            "주거정보",
            "주거, 임대, 계약, 집 구하기와 관련된 정보를 나누는 게시판",
            7,
            true,
            false,
            true,
            true,
            false
    ),
    MARKET(
            "나눔·매매",
            "개인 간 나눔, 중고거래 게시판",
            8,
            true,
            false,
            true,
            true,
            false
    ),
    FREE(
            "자유게시판",
            "자유로운 이야기와 일상 대화를 나누는 공간",
            9,
            true,
            false,
            true,
            true,
            false
    );

    private final String displayName;
    private final String defaultDescription;
    private final int displayOrder;
    private final boolean visibleInHeader;
    private final boolean adminWriteOnly;
    private final boolean commentAllowed;
    private final boolean reactionAllowed;
    private final boolean secretPostAllowed;
}