# 가힣 최종 기능 목록 재정리

이 문서는 배포 전 수동 회귀 테스트를 위해 현재 소스 기준으로 기능을 다시 정리한 문서이다. 과거 요약본은 참고하되, 최종 판단은 현재 소스 구조와 라우팅 기준으로 한다.

## 1. 서비스 구조

### 1.1 국가 커뮤니티

- 루트 `/`는 국가 선택 게이트 역할을 한다.
- 국가 커뮤니티는 `/c/{communityCode}` 경로를 사용한다.
- 현재 초기 활성 커뮤니티는 다음 2개이다.
  - `DE`: 독일
  - `JP`: 일본
- 국가별 커뮤니티 홈에서 해당 국가의 카테고리 목록과 게시판 진입 흐름을 제공한다.

### 1.2 카테고리

각 국가 커뮤니티마다 동일한 카테고리 세트가 생성된다.

| 코드 | 이름 | 주요 정책 |
| --- | --- | --- |
| `NOTICE` | 공지사항 | 관리자 작성 전용, 댓글/반응/비밀글 제한 |
| `INQUIRY` | 이용문의 | 비밀글 허용, 댓글 허용, 반응 제한 |
| `STUDY_IMMIGRATION` | 유학·이민 | 댓글/반응 허용 |
| `LOCAL_LIFE` | 지역생활 | 댓글/반응 허용 |
| `JOB` | 구인구직 | 댓글/반응 제한 |
| `TRAVEL` | 여행정보 | 댓글/반응 허용 |
| `HOUSING` | 주거정보 | 댓글/반응 허용 |
| `MARKET` | 나눔·매매 | 댓글/반응 허용, 거래 상태 기능 연동 |
| `FREE` | 자유게시판 | 댓글/반응 허용 |

## 2. 회원/인증 기능

### 2.1 회원가입

- 회원가입 페이지: `/members/signup`
- 필수 입력:
  - 아이디
  - 비밀번호
  - 비밀번호 확인
  - 닉네임
  - 이메일
  - 이용약관 동의
  - 개인정보 처리방침 동의
  - 커뮤니티 운영정책 동의
- 회원가입 이메일 인증 API:
  - `/email-auth/signup/send-code`
  - `/email-auth/signup/verify-code`
- 회원가입 전 이메일 인증 완료 여부를 검증한다.
- 아이디/닉네임/이메일 중복을 검증한다.
- 비밀번호와 비밀번호 확인 일치를 검증한다.
- 가입 성공 후 일반 회원으로 생성된다.
- 닉네임 최초 이력도 저장된다.

### 2.2 로그인/로그아웃

- 로그인 페이지: `/members/login`
- 로그아웃: `/members/logout`
- 세션 기반 로그인 구조를 사용한다.
- 로그인 성공 후 기존 요청 경로로 복귀하는 흐름이 있다.
- 삭제 회원, 탈퇴 유예 만료 회원, 로그인 불가 상태 회원은 로그인 차단된다.
- 기간 정지가 만료된 회원은 로그인 또는 접근 시 정지 해제될 수 있다.

### 2.3 아이디 찾기

- 화면: `/members/find-username`
- API:
  - `/account-recovery/username/send-code`
  - `/account-recovery/username/verify-code`
- 이메일 인증 후 아이디를 찾는 흐름이다.

### 2.4 비밀번호 찾기/재설정

- 비밀번호 찾기 화면: `/members/find-password`
- 비밀번호 재설정 화면: `/members/reset-password`
- API:
  - `/account-recovery/password/send-code`
  - `/account-recovery/password/verify-code`
- 비밀번호 재설정 세션과 토큰 해시를 사용한다.
- 재설정 성공 시 세션을 사용 처리한다.

### 2.5 마이페이지

- 전역 마이페이지: `/mypage`
- 국가별 마이페이지: `/c/{communityCode}/mypage`
- 기능:
  - 프로필 정보 확인
  - 내 활동 통계 확인
  - 최근 반응/멘션 확인
  - 내가 쓴 글 목록 진입
  - 프로필 수정
  - 비밀번호 변경
  - 회원 탈퇴 신청

### 2.6 프로필 수정/이메일 변경

- 프로필 수정 화면: `/mypage/edit`
- 이메일 변경 인증 API:
  - `/email-auth/change-email/send-code`
  - `/email-auth/change-email/verify-code`
- 닉네임 변경 정책:
  - 30일 내 재변경 제한
  - 최근 90일 내 사용된 닉네임 재사용 제한
  - 금지 닉네임 접두어/정확 일치 문구 제한
- 이메일 변경 시 인증 완료 여부를 검증한다.

### 2.7 비밀번호 변경

- 비밀번호 변경 화면: `/mypage/password`
- 현재 비밀번호 확인 후 새 비밀번호로 변경한다.

### 2.8 회원 탈퇴/복구/상태 화면

- 탈퇴 신청 화면: `/mypage/withdraw`
- 탈퇴 유예 화면: `/members/withdrawn`
- 탈퇴 복구: `/members/restore`
- 정지 회원 안내 화면: `/members/suspended`
- 탈퇴 회원은 탈퇴 유예 화면, 복구, 로그아웃만 허용된다.
- 정지 회원은 홈, 마이페이지, 로그아웃, 정지 안내, 이용문의 관련 접근만 허용된다.

## 3. 게시글 기능

### 3.1 게시글 목록

- 국가별 게시글 목록: `/c/{communityCode}/posts`
- 주요 기능:
  - 카테고리 필터
  - 키워드 검색
  - 비밀글 필터
  - 첨부파일 있음 필터
  - 나눔·매매 거래 유형/상태 필터
  - 정렬
  - 페이지 크기 20/40/60
  - 페이지네이션
  - 고정글 별도 표시
  - 현재 글 강조

### 3.2 게시글 상세

- 게시글 상세: `/c/{communityCode}/posts/{postId}`
- 주요 기능:
  - 제목/본문/작성자/작성일/수정일 표시
  - 조회수 표시
  - 첨부파일 표시
  - 이미지 미리보기
  - 댓글 목록
  - 좋아요/싫어요
  - 신고 UI
  - 상세 하단 목록
  - 이전/다음 글 네비게이션
  - 목록 검색/정렬/페이지 컨텍스트 유지
- 조회수는 같은 세션/같은 날짜 기준 중복 증가를 억제한다.
- 작성 직후 `fromCreate` 보호 흐름으로 새로고침/본인 행위에 따른 조회수 증가를 억제한다.

### 3.3 게시글 작성

- 작성 화면: `/c/{communityCode}/posts/new`
- 작성 처리: `/c/{communityCode}/posts/new`
- 로그인 필요.
- 공지사항은 관리자만 작성 가능.
- 정지 회원은 이용문의에만 작성 가능.
- 비밀글은 이용문의 카테고리에서만 작성 가능.
- 첨부파일 업로드 가능.
- 나눔·매매 카테고리에서는 거래 유형과 거래 상태 기능이 연동된다.

### 3.4 게시글 수정/삭제

- 수정 화면: `/c/{communityCode}/posts/{postId}/edit`
- 수정 처리: `/c/{communityCode}/posts/{postId}/edit`
- 삭제 처리: `/c/{communityCode}/posts/{postId}/delete`
- 작성자만 수정/삭제 가능.
- 삭제 또는 블라인드 처리된 게시글은 일반 수정/삭제 불가.
- 수정 시 기존 첨부파일과 신규 첨부파일의 총 개수/총 용량을 검증한다.

### 3.5 게시글 반응

- 좋아요: `/c/{communityCode}/posts/{postId}/like`
- 싫어요: `/c/{communityCode}/posts/{postId}/dislike`
- 로그인 필요.
- 카테고리 정책상 반응이 허용된 게시판에서만 가능하다.
- 같은 사용자의 중복 반응은 취소/전환 방식으로 처리된다.

### 3.6 나눔·매매 거래 상태

- 거래 상태 변경: `/c/{communityCode}/posts/{postId}/trade-status/toggle`
- 나눔·매매 카테고리의 거래 게시글에 적용된다.
- 작성자 또는 권한 있는 사용자 기준으로 상태 전환 여부를 확인해야 한다.

## 4. 댓글 기능

### 4.1 댓글 목록/작성

- 댓글 작성: `/c/{communityCode}/posts/{postId}/comments`
- 댓글은 게시글 상세 하단에 노출된다.
- 댓글 정렬/페이지네이션을 제공한다.
- 카테고리 정책상 댓글이 허용된 게시판에서만 작성 가능하다.
- 정지 회원은 이용문의 게시글에 대해서만 제한적으로 작성 가능하다.

### 4.2 댓글 수정/삭제

- 댓글 수정: `/c/{communityCode}/posts/{postId}/comments/{commentId}/edit`
- 댓글 삭제: `/c/{communityCode}/posts/{postId}/comments/{commentId}/delete`
- 작성자만 수정/삭제 가능.
- 삭제/블라인드 상태 댓글은 사용자 수정/삭제 제한 대상이다.

### 4.3 댓글 반응

- 댓글 좋아요: `/c/{communityCode}/comments/{commentId}/like`
- 댓글 싫어요: `/c/{communityCode}/comments/{commentId}/dislike`
- 로그인 필요.
- 카테고리 정책상 반응이 허용된 게시판에서만 가능하다.
- 중복 반응은 취소/전환 방식으로 처리된다.

### 4.4 멘션

- 댓글 작성 시 작성자/대상자 멘션 흐름을 지원한다.
- 마이페이지에서 내 멘션/최근 상호작용을 확인할 수 있다.
- 탈퇴/최종 종료 처리 시 멘션 스냅샷 비식별화 정책이 있다.

## 5. 첨부파일 기능

### 5.1 업로드 제한

- 게시글 1개당 최대 3개.
- 단일 파일 최대 10MB.
- 게시글 1개당 전체 첨부파일 최대 30MB.
- 작성폼/수정폼 프론트 검증과 서버 검증이 모두 존재한다.

### 5.2 다운로드/미리보기

- 개별 다운로드: `/c/{communityCode}/posts/attachments/{attachmentId}/download`
- 이미지 미리보기: `/c/{communityCode}/posts/attachments/{attachmentId}/preview`
- 전체 ZIP 다운로드: `/c/{communityCode}/posts/{postId}/attachments/download-all`
- 미리보기는 다운로드 횟수를 증가시키지 않는다.
- 다운로드/ZIP 다운로드 횟수는 같은 세션/같은 날짜 기준 중복 증가를 억제한다.

### 5.3 첨부파일 삭제

- 작성자 첨부파일 삭제: `/c/{communityCode}/posts/{postId}/attachments/{attachmentId}/delete`
- 관리자 첨부파일 삭제: `/c/{communityCode}/posts/{postId}/attachments/{attachmentId}/admin-delete`
- 사용자 삭제와 관리자 삭제 정책이 분리되어 있다.
- 관리자 삭제는 사용자 화면에서 제한되어야 한다.

## 6. 신고 기능

### 6.1 신고 대상

- 회원 신고
- 게시글 신고
- 댓글 신고
- 첨부파일 신고

### 6.2 신고 제한

- 본인 신고 불가.
- 관리자 신고 불가.
- 삭제/비활성 대상 신고 제한.
- 같은 대상에 대한 동일 회차 중복 신고 제한.
- 신고 가능 여부에 따라 UI 버튼 표시 여부가 달라진다.

### 6.3 신고 접수/처리

- 신고 접수: `/reports`
- 신고 시 대상 스냅샷을 기록한다.
- 신고 대상별 집계와 신고 회차를 관리한다.
- 관리자 화면에서 신고 목록과 신고자 활동 목록을 확인한다.

## 7. 관리자 기능

### 7.1 전역 관리자 홈

- `/admin`
- 전체 서비스 모니터링 성격.
- 국가별 커뮤니티 관리자 화면으로 이동 가능.

### 7.2 국가별 관리자 홈

- `/c/{communityCode}/admin`
- 특정 국가 커뮤니티에 대한 게시글/댓글/신고 조치 수행 화면.

### 7.3 게시글 관리

- 전역 게시글 관리: `/admin/posts`
- 국가별 게시글 관리: `/c/{communityCode}/admin/posts`
- 기능:
  - 검색/필터/정렬/페이지네이션
  - 고정/고정 해제
  - 블라인드
  - 복구
  - 삭제
  - 영구삭제
  - 상세 화면에서 조치 후 원래 상세로 복귀
  - 목록에서 조치 후 검색 조건 유지
  - 관리 버튼 드롭다운 UI

### 7.4 댓글 관리

- 전역 댓글 관리: `/admin/comments`
- 국가별 댓글 관리: `/c/{communityCode}/admin/comments`
- 기능:
  - 검색/정렬/페이지네이션
  - 블라인드
  - 복구
  - 삭제
  - 영구삭제
  - 목록/상세 맥락 복귀
  - 관리 버튼 드롭다운 UI

### 7.5 회원 관리

- 회원 목록: `/admin/members`
- 회원 상세: `/admin/members/{memberId}`
- 기능:
  - 검색/필터/정렬/페이지네이션
  - 회원 권한 변경
  - 활성화
  - 기간정지
  - 영구정지
  - 최종 종료
  - 닉네임 강제 변경
  - 자기 자신 조치 제한
  - 관리자 계정 보호 정책

### 7.6 신고 관리

- 전역 신고 목록: `/admin/reports`
- 국가별 신고 목록: `/c/{communityCode}/admin/reports`
- 신고자 활동 목록: `/admin/reporter-activities`
- 기능:
  - 신고 대상 확인
  - 회원 상세/게시글 상세 이동
  - 조치 필요 없음 처리
  - 신고 상태/회차 관리

### 7.7 관리자 로그

- 관리자 로그 목록: `/admin/logs`
- 기능:
  - 로그 검색
  - 기간 필터
  - 대상 타입 필터
  - 정렬
  - 페이지네이션
  - 변경 전/후 스냅샷 모달 확인
  - 사유 확인

### 7.8 닉네임 관리

- 닉네임 이력: `/admin/nickname-histories`
- 닉네임 예약: `/admin/nickname-reservations`
- 기능:
  - 닉네임 변경/예약 이력 검색
  - 최근 사용 닉네임 재사용 제한 추적

## 8. 운영/보안/배포 기능

### 8.1 CSRF

- Spring Security CSRF 활성화.
- Thymeleaf form은 hidden CSRF 토큰을 사용한다.
- fetch 기반 POST는 CSRF meta/header를 사용한다.

### 8.2 세션/권한/상태 인터셉터

- 방문자 카운트 인터셉터.
- 작성 직후 조회수 증가 보호 인터셉터.
- 로그인 세션 유효성 인터셉터.
- 로그인 필요 경로 인터셉터.
- 회원 상태 정책 인터셉터.
- 관리자 권한 인터셉터.

### 8.3 dev/prod 환경

- 기본 프로필은 `dev`.
- dev는 H2 TCP DB와 테스트 데이터 초기화.
- prod는 MySQL과 `ddl-auto=validate`.
- prod는 환경변수 기반 DB/메일/관리자 계정/업로드 경로 설정.
- `deploy/db/schema.sql`로 최초 스키마 생성.

### 8.4 정책/문서 페이지

- `/terms`: 이용약관
- `/privacy`: 개인정보 처리방침
- `/policy`: 운영정책
- `/disclaimer`: 면책조항
- `/contact`: 운영문의
- 회원가입 동의 모달은 요약, 푸터 정책 페이지는 사용자 접근 가능한 원문 역할을 한다.
- `docs/policies/*.md`는 개발/운영 관리 기준 원문이다.

## 9. 현재 주의할 점

- 루트 `README.md`와 `docs/00-overview/project-overview.md` 일부는 초기 계획 내용이 남아 있어 현재 구현 상태와 맞지 않을 수 있다.
- `application-dev.properties`는 H2 TCP 모드이므로 로컬에서 H2 서버가 켜져 있지 않으면 `./gradlew bootRun`이 실패한다.
- 최종 회귀 테스트는 `prod-like MySQL validate` 환경을 기준으로 먼저 수행하는 것이 안정적이다.
- 전체 자동 단위 테스트는 거의 없는 상태이며, 현재 배포 전에는 수동 회귀 테스트가 핵심이다.
