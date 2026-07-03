# Gahih Production Deployment Guide

이 문서는 가힣 서비스를 운영 환경에서 실행하기 위한 최소 배포 절차를 정리한다.

## 1. Deployment Principles

* 운영 환경에서는 반드시 `spring.profiles.active=prod`를 사용한다.
* 운영 DB에서는 Hibernate `ddl-auto=create` 또는 `ddl-auto=update`를 사용하지 않는다.
* 운영 DB 스키마는 `deploy/db/schema.sql`을 이용해 최초 1회 생성한다.
* 실제 비밀번호, 메일 앱 비밀번호, DB 비밀번호는 Git에 커밋하지 않는다.
* 운영 환경변수는 서버 환경변수 또는 systemd `EnvironmentFile`로 관리한다.
* `.env`, `.idea`, `.gradle`, `build`, 업로드 파일, 로그 파일은 배포 산출물이나 Git에 포함하지 않는다.

## 2. Required Environment Variables

운영 실행에 필요한 환경변수는 다음과 같다.

```env
SPRING_PROFILES_ACTIVE=prod

GAHIH_DB_URL=jdbc:mysql://localhost:3306/gahih?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
GAHIH_DB_USERNAME=gahih_user
GAHIH_DB_PASSWORD=change-me

GAHIH_MAIL_USERNAME=your-mail@example.com
GAHIH_MAIL_PASSWORD=change-me

GAHIH_ADMIN_USERNAME=admin
GAHIH_ADMIN_PASSWORD=change-me
GAHIH_ADMIN_NICKNAME=운영자
GAHIH_ADMIN_EMAIL=admin@example.com

GAHIH_UPLOAD_DIR=/var/gahih/uploads
```

`change-me`, `your-mail@example.com`, `admin@example.com` 값은 예시 값이다. 실제 운영 값은 서버에만 설정하고 Git에 커밋하지 않는다.

## 3. MySQL Database Setup

운영 DB는 `utf8mb4` 문자셋을 사용한다.

```sql
CREATE DATABASE gahih
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'gahih_user'@'localhost'
IDENTIFIED BY 'CHANGE_ME_DB_PASSWORD';

GRANT ALL PRIVILEGES ON gahih.* TO 'gahih_user'@'localhost';

FLUSH PRIVILEGES;
```

운영 DB 비밀번호는 실제 서버에서만 설정한다.

## 4. Import Initial Schema

운영 DB 최초 생성 시 `deploy/db/schema.sql`을 1회 import한다.

Linux/macOS:

```bash
mysql -u gahih_user -p gahih < deploy/db/schema.sql
```

Windows PowerShell에서는 `<` 리다이렉션이 직접 동작하지 않을 수 있으므로 `cmd /c`를 사용한다.

```powershell
cmd /c "mysql -u gahih_user -p gahih < deploy\db\schema.sql"
```

MySQL 실행 파일이 PATH에 등록되어 있지 않다면 전체 경로를 사용한다.

```powershell
cmd /c """C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"" -u gahih_user -p gahih < deploy\db\schema.sql"
```

스키마 import 후 테이블 생성 여부를 확인한다.

```sql
USE gahih;
SHOW TABLES;
```

## 5. Production-like Validation Test

운영 배포 전에는 `ddl-auto=validate` 상태에서 애플리케이션이 정상 실행되는지 확인한다.

검증용 DB 예시:

```sql
DROP DATABASE IF EXISTS gahih_validate_tmp;

CREATE DATABASE gahih_validate_tmp
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON gahih_validate_tmp.* TO 'gahih_user'@'localhost';

FLUSH PRIVILEGES;
```

스키마 import:

```powershell
cmd /c """C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"" -u gahih_user -p gahih_validate_tmp < deploy\db\schema.sql"
```

환경변수의 DB URL을 검증용 DB로 설정한다.

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:GAHIH_DB_URL="jdbc:mysql://localhost:3306/gahih_validate_tmp?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul"
$env:GAHIH_DB_USERNAME="gahih_user"
$env:GAHIH_DB_PASSWORD="change-me"

$env:GAHIH_MAIL_USERNAME="your-mail@example.com"
$env:GAHIH_MAIL_PASSWORD="change-me"

$env:GAHIH_ADMIN_USERNAME="admin"
$env:GAHIH_ADMIN_PASSWORD="change-me"
$env:GAHIH_ADMIN_NICKNAME="운영자"
$env:GAHIH_ADMIN_EMAIL="admin@example.com"

$env:GAHIH_UPLOAD_DIR="D:/gahih-uploads"
```

실행:

```powershell
.\gradlew --no-daemon bootRun --args="--spring.profiles.active=prod"
```

이때 `--spring.jpa.hibernate.ddl-auto=create`를 붙이면 안 된다. 검증 목적은 `application-prod.properties`의 `ddl-auto=validate` 상태에서 정상 실행되는지 확인하는 것이다.

확인 항목:

* 애플리케이션 정상 기동
* schema validation 오류 없음
* `/` 접근 가능
* `/c/DE` 접근 가능
* `/c/JP` 접근 가능
* `country_community` 초기 데이터 생성 확인
* `category` 초기 데이터 생성 확인
* 관리자 계정 생성 확인

초기 데이터 확인 예시:

```sql
USE gahih_validate_tmp;

SELECT code, name FROM country_community;
SELECT code, name FROM category ORDER BY id;
SELECT username, nickname, role, status FROM member;
```

## 6. Build

로컬 또는 서버에서 jar 파일을 빌드한다.

Linux/macOS:

```bash
./gradlew clean bootJar
```

Windows:

```powershell
.\gradlew clean bootJar
```

빌드 결과물은 일반적으로 `build/libs/` 아래에 생성된다.

## 7. Run Jar

jar 파일 실행 예시:

```bash
java -jar build/libs/gahih-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

실제 파일명은 `build/libs`에 생성된 jar 이름에 맞춘다.

운영에서는 직접 터미널에서 실행하기보다 systemd 서비스로 등록해서 실행한다.

## 8. Upload Directory

운영 첨부파일 저장 디렉토리를 생성하고 애플리케이션 실행 사용자에게 권한을 부여한다.

```bash
sudo mkdir -p /var/gahih/uploads
sudo chown -R gahih:gahih /var/gahih
```

운영 환경변수에는 다음 값을 사용한다.

```env
GAHIH_UPLOAD_DIR=/var/gahih/uploads
```

## 9. systemd Service Example

예시 경로:

```text
/etc/systemd/system/gahih.service
/etc/gahih/gahih.env
/opt/gahih/gahih.jar
/var/gahih/uploads
```

환경변수 파일 예시:

```env
SPRING_PROFILES_ACTIVE=prod

GAHIH_DB_URL=jdbc:mysql://localhost:3306/gahih?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
GAHIH_DB_USERNAME=gahih_user
GAHIH_DB_PASSWORD=change-me

GAHIH_MAIL_USERNAME=your-mail@example.com
GAHIH_MAIL_PASSWORD=change-me

GAHIH_ADMIN_USERNAME=admin
GAHIH_ADMIN_PASSWORD=change-me
GAHIH_ADMIN_NICKNAME=운영자
GAHIH_ADMIN_EMAIL=admin@example.com

GAHIH_UPLOAD_DIR=/var/gahih/uploads
```

`/etc/gahih/gahih.env`에는 실제 운영값을 넣는다. 이 파일은 Git에 커밋하지 않는다.

systemd 서비스 예시:

```ini
[Unit]
Description=Gahih Spring Boot Application
After=network.target mysql.service

[Service]
User=gahih
Group=gahih
WorkingDirectory=/opt/gahih
EnvironmentFile=/etc/gahih/gahih.env
ExecStart=/usr/bin/java -jar /opt/gahih/gahih.jar --spring.profiles.active=prod
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

서비스 등록 및 실행:

```bash
sudo systemctl daemon-reload
sudo systemctl enable gahih
sudo systemctl start gahih
sudo systemctl status gahih
```

로그 확인:

```bash
journalctl -u gahih -f
```

## 10. Nginx Reverse Proxy Example

Nginx 예시 설정:

```nginx
server {
    listen 80;
    server_name example.com www.example.com;

    location / {
        proxy_pass http://127.0.0.1:8080;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

운영에서는 HTTPS 적용 후 443 설정을 추가한다.

`application-prod.properties`에는 reverse proxy 환경을 고려해 다음 설정을 둔다.

```properties
server.forward-headers-strategy=framework
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=lax
```

HTTPS 적용 후에는 다음 설정을 추가 검토한다.

```properties
server.servlet.session.cookie.secure=true
```

단, `secure=true`는 HTTPS 환경에서만 세션 쿠키가 정상 동작하므로 로컬 HTTP 테스트에는 주의한다.

## 11. Post-deployment Smoke Test

배포 후 다음 항목을 확인한다.

* 홈 화면 접근
* 독일 커뮤니티 접근: `/c/DE`
* 일본 커뮤니티 접근: `/c/JP`
* 관리자 로그인
* 회원가입 인증 메일 발송
* 게시글 작성
* 댓글 작성
* 첨부파일 업로드
* 첨부파일 다운로드
* ZIP 다운로드
* 좋아요/싫어요
* 신고
* 관리자 게시글 조치
* 관리자 댓글 조치
* 관리자 회원 조치
* 관리자 신고 조치
* 관리자 로그 기록
* 서버 재시작 후 DB 데이터 유지
* 서버 재시작 후 첨부파일 유지

## 12. Backup

초기 운영 단계에서는 최소한 다음을 백업한다.

* MySQL dump
* 첨부파일 디렉토리
* 운영 환경변수 파일

MySQL 백업 예시:

```bash
mysqldump -u gahih_user -p --single-transaction --routines --triggers gahih > gahih_backup.sql
```

첨부파일 백업 예시:

```bash
tar -czf gahih_uploads_backup.tar.gz /var/gahih/uploads
```

## 13. Important Notes

* 운영 DB에서 `ddl-auto=create`를 실행하지 않는다.
* 운영 DB에서 `ddl-auto=update`를 사용하지 않는다.
* 운영 DB 최초 생성은 `deploy/db/schema.sql`로 처리한다.
* 실제 `.env` 또는 systemd `EnvironmentFile`은 Git에 커밋하지 않는다.
* `.idea`, `.gradle`, `build`, 업로드 파일, 로그 파일은 배포 산출물에 포함하지 않는다.
* Gmail 앱 비밀번호는 유출 의심 시 즉시 폐기하고 재발급한다.
* 관리자 초기 비밀번호는 최초 배포 후 반드시 안전하게 보관한다.
* 운영 서버에서 HTTPS 적용 후 `server.servlet.session.cookie.secure=true` 적용을 검토한다.
