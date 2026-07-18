# 가힣 (GAHIH)

가힣는 해외 거주 한인과 한국에 관심 있는 사용자를 위한 **국가별 커뮤니티 서비스**입니다.

프로젝트 이름은 한글의 시작과 끝인 `가`와 `힣`에서 가져왔으며, 다양한 국가와 상황에 놓인 사용자가 한국어 기반으로 정보를 나누는 공간을 목표로 합니다.

- 운영 주소: https://gahih.com
- 현재 운영 배포 태그: `v0.1.8`
- 초기 국가 커뮤니티: 독일(DE), 일본(JP)
- 현재 상태: 1차 MVP 기능 동결 및 운영 배포 완료

## 1. 프로젝트 목표

가힣는 단순 게시판 CRUD를 넘어, 실제 서비스 운영에 필요한 흐름을 직접 설계하고 구현하는 것을 목표로 한 포트폴리오 프로젝트입니다.

주요 목표는 다음과 같습니다.

- 국가별 커뮤니티 구조 설계
- 회원가입, 로그인, 이메일 인증, 계정 복구 흐름 구현
- 게시글, 댓글, 첨부파일, 반응, 신고 기능 구현
- 비밀글, 고정글, 구인구직 댓글 제한 등 게시판별 운영 정책 구현
- 관리자 조치, 신고 처리, 관리자 로그 기록 구현
- 개발 환경과 운영 환경 분리
- 운영 DB 스키마 검증 및 배포 전 회귀 테스트 수행
- 실제 도메인, HTTPS, Nginx, MySQL, systemd 기반 운영 배포 경험

## 2. AI 활용 고지

이 프로젝트는 ChatGPT의 도움을 받아 설계와 구현을 진행한 AI-assisted 포트폴리오 프로젝트입니다.

프로젝트의 주제 선정, 요구사항 정리, 기능 검증, 수동 테스트, 문서화, 배포 준비를 스스로 수행하면서, 구현 과정에서는 ChatGPT를 활용해 코드 작성, 구조 개선, 정책 설계, 오류 해결에 많은 도움을 받았습니다.

현재 버전은 실제로 구동 가능한 서비스를 완성하고 배포·운영 흐름을 경험하는 데 초점을 두었으며, 배포 이후에도 프로젝트 구조와 구현 내용을 지속적으로 학습하고 개선해 나갈 예정입니다.

## 3. 기술 스택

### Backend

- Java 17
- Spring Boot 3.5.x
- Spring MVC
- Spring Data JPA
- Spring Security
- QueryDSL
- Thymeleaf
- Lombok
- Jakarta Validation

### Database

- H2 Database: 개발 환경
- MySQL 8: 운영 환경

### Build / Runtime / Operation

- Gradle
- Spring Boot executable JAR
- Session 기반 인증
- CSRF 보호
- 환경변수 기반 운영 설정
- AWS Lightsail
- Ubuntu 24.04 LTS
- Nginx reverse proxy
- Let’s Encrypt HTTPS
- systemd 서비스 관리
- MySQL 운영 DB

### Frontend

- Thymeleaf 서버 사이드 렌더링
- HTML / CSS / JavaScript
- 기능별 CSS/JS 분리
- 반응형 UI 일부 적용

## 4. 주요 기능

### 회원 및 인증

- 회원가입
- 로그인 / 로그아웃
- 세션 기반 로그인 유지
- 이메일 인증
- 아이디 찾기
- 비밀번호 찾기
- 내 정보 수정
- 비밀번호 변경
- 회원 탈퇴
- 닉네임 변경 제한 정책
- 정지 / 탈퇴 / 최종 종료 회원 상태 관리

### 국가별 커뮤니티

- 국가 선택 게이트
- 독일 / 일본 커뮤니티 초기 구성
- 국가별 게시판 목록
- 국가별 관리자 화면
- 전역 관리자 모니터링 화면

### 게시글

- 게시글 작성 / 조회 / 수정 / 삭제
- 카테고리별 게시판
- 검색 / 정렬 / 페이지네이션
- 고정글
- 비밀글
- 조회수 정책
- 이전 / 다음 글 이동
- 상세 하단 목록 컨텍스트 유지

### 댓글 및 반응

- 댓글 작성 / 수정 / 삭제
- 댓글 정렬 / 페이지네이션
- 게시글 좋아요 / 싫어요
- 댓글 좋아요 / 싫어요
- 댓글 mention 기능
- 게시판 정책에 따른 mention 제한
- 구인구직 게시판 댓글 / 반응 제한

### 첨부파일

- 파일 업로드
- 이미지 미리보기
- 첨부파일 다운로드
- ZIP 다운로드
- 파일 크기 제한
- 다운로드 횟수 기록
- 첨부파일 신고

### 신고 및 관리자 기능

- 게시글 신고
- 댓글 신고
- 첨부파일 신고
- 작성자 신고
- 중복 신고 제한
- 신고 처리
- 게시글 블라인드 / 복구 / 삭제 / 영구삭제
- 댓글 블라인드 / 복구 / 삭제 / 영구삭제
- 회원 정지 / 해제 / 역할 변경 / 최종 종료
- 관리자 로그 기록
- 관리자 조치 사유 기록
- 관리자 로그 snapshot 조회

### 운영 정책

- 비밀글은 작성자와 관리자 중심으로 열람 제한
- 이용문의 게시판은 비밀글과 관리자 응답 흐름 지원
- 구인구직 게시판은 비방과 분쟁 방지를 위해 댓글과 반응 제한
- 관리자 mention은 운영 정책에 맞게 제한
- 탈퇴 회원 콘텐츠와 닉네임 노출 정책 분리
- 오류 페이지에서 로그인 상태와 국가 컨텍스트 유지
- 잘못된 요청 형식은 400 오류로 분류

## 5. 운영 환경 구성

운영 환경에서는 `prod` 프로필을 사용합니다.

- 운영 DB: MySQL 8
- Hibernate DDL: `validate`
- 운영 스키마: `deploy/db/schema.sql`
- 파일 업로드 경로: 환경변수 `GAHIH_UPLOAD_DIR`
- 메일 계정: 환경변수로 관리
- 관리자 초기 계정: 환경변수로 관리
- 운영 환경변수 파일: `/etc/gahih/gahih.env`
- 업로드 경로: `/var/lib/gahih/uploads`
- Spring Boot 앱: `127.0.0.1:8080`에만 바인딩
- 외부 공개 포트: Nginx 80/443
- MySQL 3306: localhost 바인딩

운영 실행 절차는 별도 문서에 정리되어 있습니다.

```text
deploy/README.md
```

## 6. 배포 구조

가힣는 다음 구조로 배포되었습니다.

```text
Client
  ↓
https://gahih.com
  ↓
Nginx :80/:443
  ↓ reverse proxy
Spring Boot :127.0.0.1:8080
  ↓
MySQL :127.0.0.1:3306
```

운영 배포에서 중점적으로 확인한 항목은 다음과 같습니다.

- 도메인 연결 및 HTTPS 적용
- `https://gahih.com` canonical 주소 고정
- `www` 및 `http` 요청의 301 redirect
- Nginx reverse proxy 구성
- systemd 기반 Spring Boot JAR 실행
- dev/prod 프로필 분리
- 환경변수 기반 비밀값 관리
- 운영 DB schema validation
- DB 백업 및 Lightsail snapshot 생성
- UFW 방화벽과 외부 노출 포트 점검

## 7. 현재 운영 배포 상태

현재 운영 배포 버전은 `v0.1.8`입니다.

`v0.1.8`은 홈 화면 안내 문구만 보강한 패치로, DB 스키마·운영 데이터·업로드 파일에는 변경이 없습니다.

### 주요 배포 후 수정 이력

- `v0.1.4`: 이메일 인증 타이머 표시 버그 수정
- `v0.1.5`: 이미 인증 완료된 이메일에 대한 인증코드 재발송 방지
- `v0.1.6`: Spring Security 기본 개발용 사용자 생성 경고 제거
- `v0.1.7`: Gradle wrapper 실행 권한 정리
- `v0.1.8`: 홈 화면 서비스 소개 문구 보강

### 최종 clean 상태

- DB에는 관리자 계정 1개만 유지
- 게시글 / 댓글 / 첨부파일 / 신고 / 관리자 로그 / 이메일 인증 기록 / 방문 통계 초기화
- `member` 테이블 `AUTO_INCREMENT = 2`
- uploads 폴더 비움
- 최종 DB 백업 생성
- 최종 uploads 백업 생성
- 최종 Lightsail snapshot 생성

### 최종 스모크 테스트

다음 항목을 확인했습니다.

- `gahih.service` active running
- `nginx` active running
- `mysql` active running
- `https://gahih.com` 200
- `https://www.gahih.com` → `https://gahih.com/` 301 redirect
- `http://gahih.com` → `https://gahih.com/` 301 redirect
- `http://www.gahih.com` → `https://gahih.com/` 301 redirect
- 공개 페이지 접근 정상
- 로그인 / 회원가입 / 아이디 찾기 / 비밀번호 찾기 페이지 접근 정상
- 비로그인 관리자 접근 시 로그인 페이지로 redirect
- Nginx에서 `/wp-admin`, `/.env`, `/.git`, `/phpmyadmin` 등 스캐너 경로 404 차단
- 최근 앱 로그에 `Using generated security password`, `ERROR`, 반복 `Exception` 없음

## 8. 프로젝트에서 중점적으로 다룬 문제

이 프로젝트는 단순히 게시글을 작성하고 조회하는 기능보다, 실제 서비스를 운영할 때 발생하는 상태와 정책을 다루는 데 초점을 두었습니다.

예를 들어 다음 문제들을 직접 설계하고 구현했습니다.

- 탈퇴 회원의 콘텐츠를 어떻게 보존하거나 표시할 것인가
- 관리자 삭제와 사용자 삭제를 어떻게 구분할 것인가
- 비밀글을 누구에게 어디까지 보여줄 것인가
- 신고 대상과 신고자를 어떻게 기록할 것인가
- 관리자 조치의 사유와 당시 상태를 어떻게 남길 것인가
- 게시판별로 댓글, 반응, mention 정책을 어떻게 다르게 적용할 것인가
- 개발 환경과 운영 환경을 어떻게 분리할 것인가
- 운영 DB 스키마를 어떻게 검증할 것인가
- 공개 인터넷 노출 후 봇/스캐너 요청을 어떻게 줄일 것인가
- 방문자 통계가 봇 요청에 오염될 때 어떤 기준으로 운영 지표를 잡을 것인가

## 9. 트러블슈팅 사례

### 이메일 인증 타이머 UTC/KST 문제

서버에서 `LocalDateTime`으로 내려준 만료 시간을 브라우저가 시간대 정보 없이 해석하면서, 운영 환경에서 인증코드 만료 메시지가 잘못 표시될 수 있었습니다.

이를 해결하기 위해 API 응답에 `remainingSeconds`를 추가하고, 프론트엔드는 절대 시간 문자열이 아니라 남은 초 단위로 타이머를 표시하도록 변경했습니다.

### 이메일 인증 완료 후 재발송 문제

이미 인증이 완료된 같은 이메일에 대해 다시 인증코드 보내기 버튼을 누르면 새 코드가 발급될 수 있었습니다.

이를 같은 이메일 / 같은 인증 목적에서 이미 인증 완료된 경우 재발송하지 않고, “이미 인증이 완료되었습니다. 다음 단계를 진행해주세요.” 메시지를 반환하도록 개선했습니다.

### 방문자 통계 오염 문제

공개 인터넷에 노출된 뒤 스캐너와 봇 요청이 유입되면서 단순 쿠키 기반 방문자 통계가 부정확해질 수 있었습니다.

현재 기본 집계 방식은 `LOGIN_MEMBER_DAILY`로 전환하여, 로그인한 일반 회원만 한국 시간 기준 하루 1회 집계하도록 변경했습니다. 향후 필요 시 property 설정으로 기존 공개 쿠키 기반 방식과 스위칭할 수 있도록 설계했습니다.

### Spring Security generated password 경고

가힣는 자체 회원 로그인 + 세션 + 인터셉터 방식으로 인증을 처리하지만, Spring Boot가 기본 개발용 사용자를 자동 생성하면서 운영 로그에 generated password 경고가 출력되었습니다.

기본 `UserDetailsServiceAutoConfiguration`만 제외하여 경고를 제거하고, CSRF와 보안 헤더 등 기존 SecurityFilterChain은 유지했습니다.

### Nginx 봇/스캐너 경로 차단

`/wp-admin`, `/.env`, `/.git`, `/phpmyadmin`, `*.php`, `*.sql`, `*.bak` 등 흔한 스캐너 경로를 Nginx에서 404로 차단하도록 설정했습니다.

이를 통해 공격성 요청이 Spring Boot 앱까지 전달되지 않도록 하고, 로그 노이즈와 불필요한 앱 부하를 줄였습니다.

### MySQL 백업 권한 문제

운영 DB 백업 중 `mysqldump`가 tablespace 관련 권한 문제로 실패했습니다.

`--no-tablespaces` 옵션을 추가하여 애플리케이션 복구에 필요한 DB 덤프를 정상 생성하도록 조정했습니다.

## 10. 문서 구조

```text
docs/
 ├ 00-overview/
 │  ├ project-overview.md
 │  ├ portfolio-summary.md
 │  ├ directory-structure.md
 │  └ THIRD-PARTY-NOTICES.md
 ├ policies/
 │  ├ terms.md
 │  ├ privacy-policy.md
 │  └ operation-policy.md
 └ test/
    ├ 01-feature-inventory.md
    ├ 02-core-flows.md
    ├ 03-final-regression-checklist.md
    ├ 04-test-result-template.md
    └ 05-test-priority-and-ui-polish.md
```

## 11. 향후 개선 가능성

현재 버전은 포트폴리오용 MVP와 운영 가능성을 목표로 합니다.

향후 개선 가능한 항목은 다음과 같습니다.

- 모바일 UI 추가 개선
- 테스트 코드 보강
- Docker 기반 실행 환경 추가
- 배포 자동화
- 알림 기능 고도화
- 검색 기능 고도화
- 관리자 대시보드 지표 확장
- GA4 / Search Console 기반 유입 분석
- Cloudflare 또는 WAF 기반 봇 방어 고도화

## 12. 작성자

Kibaek Lee
