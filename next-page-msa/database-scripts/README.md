# Next-Page MSA Database Setup Guide

## 개요

이 디렉토리는 Next-Page 프로젝트의 Monolithic 아키텍처를 MSA로 전환하기 위한 데이터베이스 분리 스크립트를 포함합니다.

## 데이터베이스 구조

```
Monolithic (next_page_db)
    ↓ 분리
    ├─ next_page_member     (Member Service)
    ├─ next_page_story      (Story Service)
    └─ next_page_reaction   (Reaction Service)
```

## 계정 구조

- **Admin/Root**: `root` / `mariadb` (기존 관리자 계정)
- **Application User**: `swcamp` / `swcamp` (모든 MSA 서비스가 사용하는 계정)

## 실행 순서

### 0단계: 계정 및 권한 설정 (Root 실행)

`swcamp` 계정을 생성하고 권한을 부여합니다.

```bash
mysql -u root -pmariadb < 00-init-roles.sql
```

### 1단계: 데이터베이스 생성 (swcamp 실행)

이제부터는 `swcamp` 계정으로 실행합니다.

```bash
mysql -u swcamp -pswcamp < 01-create-databases.sql
```

**생성되는 항목:**

- 데이터베이스 3개 생성 `next_page_member`, `next_page_story`, `next_page_reaction`

---

### 2단계: Member Service 스키마 생성

```bash
mysql -u swcamp -pswcamp < 02-member-service-schema.sql
```

**생성되는 테이블:**

- `users` (회원 정보)
- `refresh_tokens` (JWT 리프레시 토큰)

**샘플 데이터:**

- 관리자 계정: <admin@nextpage.com> / password123
- 일반 사용자 3명 (user1, user2, user3)

---

### 3단계: Story Service 스키마 생성

```bash
mysql -u swcamp -pswcamp < 03-story-service-schema.sql
```

**생성되는 테이블:**

- `categories` (소설 카테고리)
- `books` (릴레이 소설 방)
- `sentences` (소설 문장)

---

### 4단계: Reaction Service 스키마 생성

```bash
mysql -u swcamp -pswcamp < 04-reaction-service-schema.sql
```

**생성되는 테이블:**

- `comments` (댓글/대댓글)
- `book_votes` (소설 투표)
- `sentence_votes` (문장 투표)

---

## 일괄 실행 스크립트 (Bash)

### Linux/Mac

```bash
#!/bin/bash
ROOT_PASS="mariadb"
APP_USER="swcamp"
APP_PASS="swcamp"

echo "0. 계정 및 권한 설정 (ROOT)..."
mysql -u root -p$ROOT_PASS < 00-init-roles.sql

echo "1. 데이터베이스 생성 ($APP_USER)..."
mysql -u $APP_USER -p$APP_PASS < 01-create-databases.sql

echo "2. Member Service 스키마 생성..."
mysql -u $APP_USER -p$APP_PASS < 02-member-service-schema.sql

echo "3. Story Service 스키마 생성..."
mysql -u $APP_USER -p$APP_PASS < 03-story-service-schema.sql

echo "4. Reaction Service 스키마 생성..."
mysql -u $APP_USER -p$APP_PASS < 04-reaction-service-schema.sql

echo "✅ 모든 데이터베이스 설정 완료!"
```

### Windows (PowerShell)

```powershell
$ROOT_PASS = "mariadb"
$APP_USER = "swcamp"
$APP_PASS = "swcamp"

Write-Host "0. 계정 및 권한 설정 (ROOT)..." -ForegroundColor Green
Get-Content 00-init-roles.sql | mysql -u root -p$ROOT_PASS

Write-Host "1. 데이터베이스 생성 ($APP_USER)..." -ForegroundColor Green
Get-Content 01-create-databases.sql | mysql -u $APP_USER -p$APP_PASS

Write-Host "2. Member Service 스키마 생성..." -ForegroundColor Green
Get-Content 02-member-service-schema.sql | mysql -u $APP_USER -p$APP_PASS

Write-Host "3. Story Service 스키마 생성..." -ForegroundColor Green
Get-Content 03-story-service-schema.sql | mysql -u $APP_USER -p$APP_PASS

Write-Host "4. Reaction Service 스키마 생성..." -ForegroundColor Green
Get-Content 04-reaction-service-schema.sql | mysql -u $APP_USER -p$APP_PASS

Write-Host "✅ 모든 데이터베이스 설정 완료!" -ForegroundColor Cyan
```
