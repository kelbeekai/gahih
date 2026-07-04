# 가힣 최종 회귀 테스트 결과 기록

## 1. 테스트 개요

| 항목 | 내용 |
| --- | --- |
| 테스트 일자 | 2026-__-__ |
| 테스트 담당 |  |
| 브랜치/커밋 |  |
| 실행 프로필 | prod / dev |
| DB | MySQL `gahih_validate_tmp` / 기타 |
| 브라우저 | Chrome / Edge / 기타 |
| OS | Windows 11 / 기타 |
| 테스트 기준 문서 | `docs/test/03-final-regression-checklist.md` |

## 2. 실행 환경

```text
SPRING_PROFILES_ACTIVE=prod
DB: MySQL
JPA ddl-auto: validate
Upload dir: 
Mail: 실제 발송 / 발송 생략 / 테스트 계정
```

## 3. 테스트 계정

실제 비밀번호는 기록하지 않는다.

| 역할 | 아이디 | 비고 |
| --- | --- | --- |
| 관리자 |  |  |
| 일반 회원 A |  | 작성자 |
| 일반 회원 B |  | 신고자/타 사용자 |
| 정지 회원 |  | 관리자 조치로 생성 |
| 탈퇴 유예 회원 |  | 탈퇴 신청으로 생성 |

## 4. 요약 결과

| 구분 | 전체 | 통과 | 실패 | 보류 | 비고 |
| --- | ---: | ---: | ---: | ---: | --- |
| P0 |  |  |  |  |  |
| P1 |  |  |  |  |  |
| P2 |  |  |  |  |  |

## 5. 주요 플로우 결과

| 플로우 | 결과 | 비고 |
| --- | --- | --- |
| 앱 실행/기본 접근 | PASS / FAIL / HOLD |  |
| 회원가입/로그인 | PASS / FAIL / HOLD |  |
| 계정 복구 | PASS / FAIL / HOLD |  |
| 게시글 CRUD | PASS / FAIL / HOLD |  |
| 댓글/반응 | PASS / FAIL / HOLD |  |
| 첨부파일 | PASS / FAIL / HOLD |  |
| 신고 | PASS / FAIL / HOLD |  |
| 관리자 게시글/댓글 | PASS / FAIL / HOLD |  |
| 관리자 회원/신고/로그 | PASS / FAIL / HOLD |  |
| 회원 상태 정책 | PASS / FAIL / HOLD |  |
| 국가별 분리 | PASS / FAIL / HOLD |  |
| 정책 페이지 | PASS / FAIL / HOLD |  |

## 6. 발견 이슈

| ID | 우선순위 | 화면/기능 | 재현 절차 | 기대 결과 | 실제 결과 | 상태 |
| --- | --- | --- | --- | --- | --- | --- |
| BUG-001 | P0/P1/P2 |  | 1.  2.  3.  |  |  | OPEN/FIXED/HOLD |

## 7. UI/문구 개선 메모

| ID | 화면 | 개선 내용 | 우선순위 | 처리 여부 |
| --- | --- | --- | --- | --- |
| UI-001 |  |  | 높음/중간/낮음 | 미처리/처리/보류 |

## 8. 최종 판단

```text
최종 판단: PASS / CONDITIONAL PASS / FAIL

배포 가능 여부:
- [ ] 가능
- [ ] 조건부 가능
- [ ] 불가

조건부 배포라면 남은 조건:
1. 
2. 
3. 
```

## 9. 다음 조치

- [ ] 실패한 P0 항목 수정.
- [ ] 실패한 P1 항목 중 실제 운영에 치명적인 항목 수정.
- [ ] 단순 UI 아쉬움은 별도 개선 목록으로 분리.
- [ ] `clean bootJar` 재실행.
- [ ] Git 상태 clean 확인.
