# 가힣 프로젝트 개요

## 1. 프로젝트 소개
가힣은 전 세계 한인들을 위한 커뮤니티 서비스를 목표로 하는 웹 프로젝트이다.
1차 버전은 게시판 중심의 커뮤니티 기능을 구현하는 것을 목표로 한다.

## 2. 1차 버전 목표
- 회원가입
- 로그인 / 로그아웃
- 게시글 작성 / 조회 / 수정 / 삭제
- 카테고리 분류
- 마이페이지
- 관리자 페이지 기초 기능

## 3. 프로젝트 핵심 가치
- 초보 개발자도 끝까지 구현 가능한 구조
- 단순 CRUD가 아니라 인증/인가까지 포함된 서비스 흐름 구현
- 나중에 국제화, 채팅, 쇼핑몰 기능으로 확장 가능한 구조 지향

## 4. 사용 기술
- Backend: Java, Spring Boot, Spring MVC, Spring Data JPA, Spring Security
- Database: H2 / MySQL
- Frontend: Thymeleaf
- Build Tool: Gradle

## 5. 현재 구현 상태
- [ ] 회원가입
- [ ] 로그인
- [ ] 로그아웃
- [ ] 게시글 CRUD
- [ ] 마이페이지
- [ ] 관리자 페이지

## 6. 디렉토리와 문서 읽는 순서
1. project-overview.md
2. directory-structure.md
3. request-flow.md
4. member.md
5. signup.md
6. login.md
7. post-create.md

## 7. 이 프로젝트를 통해 연습하는 것
- 계층형 구조 설계
- 엔티티와 DTO의 역할 분리
- 인증과 인가의 차이
- JPA 연관관계 관리
- 템플릿 엔진 기반 서버사이드 렌더링
- 실서비스를 고려한 구조화 습관