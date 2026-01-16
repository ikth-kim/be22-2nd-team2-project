# Next-Page MSA Database Setup Guide

## 📖 개요

이 디렉토리는 Next-Page 프로젝트의 Monolithic 아키텍처를 MSA로 전환하기 위한 데이터베이스 분리 스크립트를 포함합니다.
MSA의 핵심 원칙인 **Database per Service** 패턴을 적용하여 각 도메인 서비스가 독립적인 데이터베이스를 소유합니다.

**Last Updated:** 2026-01-16

## ⚡ Quick Start (Windows PowerShell)

데이터베이스 설치를 한 번에 완료하려면 `database-scripts` 디렉토리에서 아래 명령을 실행하세요.

```powershell
$ROOT_PASS = "mariadb"
$APP_USER = "swcamp"
$APP_PASS = "swcamp"

Get-Content 00-init-roles.sql | mysql -u root -p$ROOT_PASS
Get-Content 01-create-databases.sql | mysql -u $APP_USER -p$APP_PASS
Get-Content 02-member-service-schema.sql | mysql -u $APP_USER -p$APP_PASS
Get-Content 03-story-service-schema.sql | mysql -u $APP_USER -p$APP_PASS
Get-Content 04-reaction-service-schema.sql | mysql -u $APP_USER -p$APP_PASS
```

## 🏗️ 데이터베이스 구조

### Database per Service 패턴

```
Monolithic (next_page_db)
    ↓ MSA 전환 (도메인별 분리)
    ├─ next_page_member     (Member Service)
    │   ├── users                    ← 회원 정보
    │   └── refresh_token            ← JWT Refresh Token
    │
    ├─ next_page_story      (Story Service)
    │   ├── categories               ← 소설 카테고리
    │   ├── books                    ← 릴레이 소설방
    │   └── sentences                ← 소설 문장
    │
    └─ next_page_reaction   (Reaction Service)
        ├── comments                 ← 댓글/대댓글
        ├── book_votes               ← 소설 투표
        └── sentence_votes           ← 문장 투표
```

### 주요 특징
- **독립성**: 각 서비스는 자신의 데이터베이스만 접근 가능
- **논리적 참조**: 서비스 간 데이터는 ID로만 참조 (Foreign Key 없음)
- **트랜잭션 경계**: 각 서비스 내부에서만 트랜잭션 관리
- **확장성**: 서비스별로 독립적인 스케일링 및 DB 기술 선택 가능

## 🔐 계정 구조

- **Admin/Root**: `root` / `mariadb` (기존 관리자 계정)
  - 용도: 데이터베이스 생성, 사용자 권한 관리

- **Application User**: `swcamp` / `swcamp` (모든 MSA 서비스가 사용하는 계정)
  - 용도: 애플리케이션 레벨 CRUD 작업
  - 권한: 3개 데이터베이스에 대한 ALL PRIVILEGES

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

#### 1. `users` (회원 정보)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| user_id | BIGINT | PK, AUTO_INCREMENT | 회원 고유 ID |
| user_email | VARCHAR(255) | UNIQUE, NOT NULL | 이메일 (로그인 ID) |
| user_pw | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 (BCrypt) |
| user_nicknm | VARCHAR(50) | UNIQUE, NOT NULL | 닉네임 |
| user_role | VARCHAR(20) | NOT NULL | 역할 (USER/ADMIN) |
| user_status | VARCHAR(20) | NOT NULL | 상태 (ACTIVE/PENDING/INACTIVE) |
| user_created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 가입일시 |
| user_updated_at | DATETIME | ON UPDATE | 수정일시 |

**인덱스:**
- `idx_email`: 이메일 조회 최적화
- `idx_nickname`: 닉네임 조회 최적화
- `idx_status`: 상태별 조회 최적화

#### 2. `refresh_token` (JWT Refresh Token)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| token_id | BIGINT | PK, AUTO_INCREMENT | 토큰 ID |
| user_id | BIGINT | NOT NULL | 회원 ID (논리적 참조) |
| token | VARCHAR(500) | UNIQUE, NOT NULL | Refresh Token 값 |
| expires_at | DATETIME | NOT NULL | 만료 시간 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 생성 시간 |

**샘플 데이터:**
- 관리자 계정: admin@nextpage.com / password123 (ADMIN, ACTIVE)
- 일반 사용자 3명: user1@test.com, user2@test.com, user3@test.com (USER, ACTIVE)

---

### 3단계: Story Service 스키마 생성

```bash
mysql -u swcamp -pswcamp < 03-story-service-schema.sql
```

**생성되는 테이블:**

#### 1. `categories` (소설 카테고리)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| category_id | VARCHAR(50) | PK | 카테고리 ID |
| category_nm | VARCHAR(100) | NOT NULL | 카테고리 이름 |

**샘플 데이터:**
- FANTASY (판타지), ROMANCE (로맨스), THRILLER (스릴러), SF (SF), COMEDY (코미디), HORROR (공포)

#### 2. `books` (릴레이 소설방)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| book_id | BIGINT | PK, AUTO_INCREMENT | 소설 ID |
| writer_id | BIGINT | NOT NULL | 작성자 ID (논리적 참조: Member Service) |
| title | VARCHAR(200) | NOT NULL | 소설 제목 |
| category_id | VARCHAR(50) | FK | 카테고리 ID |
| max_sequence | INT | NOT NULL | 목표 문장 수 |
| current_sequence | INT | DEFAULT 0 | 현재 문장 수 |
| status | VARCHAR(20) | NOT NULL | 상태 (ONGOING/COMPLETED) |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | ON UPDATE | 수정일시 |

**인덱스:**
- `idx_writer_id`: 작성자별 소설 조회
- `idx_category`: 카테고리별 조회
- `idx_status`: 상태별 조회
- `idx_created_at`: 최신순 정렬

#### 3. `sentences` (소설 문장)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| sentence_id | BIGINT | PK, AUTO_INCREMENT | 문장 ID |
| book_id | BIGINT | FK, NOT NULL | 소설 ID |
| writer_id | BIGINT | NOT NULL | 작성자 ID (논리적 참조: Member Service) |
| content | TEXT | NOT NULL | 문장 내용 |
| sequence_num | INT | NOT NULL | 문장 순서 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 작성일시 |
| updated_at | DATETIME | ON UPDATE | 수정일시 |

**인덱스:**
- `idx_book_sequence`: 소설별 순서 조회 최적화
- `idx_writer_id`: 작성자별 문장 조회

**제약조건:**
- UNIQUE(book_id, sequence_num): 소설 내 문장 순서 중복 방지

---

### 4단계: Reaction Service 스키마 생성

```bash
mysql -u swcamp -pswcamp < 04-reaction-service-schema.sql
```

**생성되는 테이블:**

#### 1. `comments` (댓글/대댓글)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| comment_id | BIGINT | PK, AUTO_INCREMENT | 댓글 ID |
| book_id | BIGINT | NOT NULL | 소설 ID (논리적 참조: Story Service) |
| writer_id | BIGINT | NOT NULL | 작성자 ID (논리적 참조: Member Service) |
| parent_id | BIGINT | NULL | 부모 댓글 ID (대댓글인 경우) |
| content | TEXT | NOT NULL | 댓글 내용 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 작성일시 |
| updated_at | DATETIME | ON UPDATE | 수정일시 |

**인덱스:**
- `idx_book_id`: 소설별 댓글 조회
- `idx_parent_id`: 대댓글 조회
- `idx_writer_id`: 작성자별 댓글 조회

**계층 구조:**
- parent_id가 NULL이면 최상위 댓글
- parent_id가 있으면 대댓글 (2레벨까지만 허용)

#### 2. `book_votes` (소설 투표)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| vote_id | BIGINT | PK, AUTO_INCREMENT | 투표 ID |
| book_id | BIGINT | NOT NULL | 소설 ID (논리적 참조: Story Service) |
| voter_id | BIGINT | NOT NULL | 투표자 ID (논리적 참조: Member Service) |
| vote_type | VARCHAR(20) | NOT NULL | 투표 타입 (LIKE/DISLIKE) |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 투표일시 |

**인덱스:**
- `idx_book_voter`: 소설별 투표자 조회
- `idx_book_id`: 소설별 투표 집계

**제약조건:**
- UNIQUE(book_id, voter_id): 한 소설에 중복 투표 방지

#### 3. `sentence_votes` (문장 투표)
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| vote_id | BIGINT | PK, AUTO_INCREMENT | 투표 ID |
| sentence_id | BIGINT | NOT NULL | 문장 ID (논리적 참조: Story Service) |
| voter_id | BIGINT | NOT NULL | 투표자 ID (논리적 참조: Member Service) |
| vote_type | VARCHAR(20) | NOT NULL | 투표 타입 (LIKE/DISLIKE) |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 투표일시 |

**인덱스:**
- `idx_sentence_voter`: 문장별 투표자 조회
- `idx_sentence_id`: 문장별 투표 집계

**제약조건:**
- UNIQUE(sentence_id, voter_id): 한 문장에 중복 투표 방지

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

---

## 🔗 서비스 간 데이터 참조 전략

### 논리적 참조 (Logical Reference)

MSA 환경에서는 서비스 간 데이터베이스 레벨의 Foreign Key를 사용하지 않습니다. 대신 **논리적 참조**를 사용합니다.

#### 예시: Story Service의 books 테이블
```sql
CREATE TABLE books (
    book_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    writer_id BIGINT NOT NULL,  -- ⚠️ FK 없음! Member Service의 user_id를 논리적으로 참조
    title VARCHAR(200) NOT NULL,
    -- ...
);
```

#### 데이터 일관성 보장 방법

1. **Application Level Validation**
   - Story Service에서 소설 생성 시 Member Service의 Internal API를 호출하여 writer_id 검증
   ```java
   // MemberServiceClient를 통해 회원 존재 여부 확인
   ApiResponse<Boolean> exists = memberServiceClient.memberExists(writerId);
   if (!exists.getData()) {
       throw new MemberNotFoundException("존재하지 않는 회원입니다.");
   }
   ```

2. **Eventual Consistency (최종 일관성)**
   - 즉시 일관성이 필요하지 않은 경우 비동기 메시징 사용 (향후 Kafka/RabbitMQ 도입 예정)
   - 예: 회원 탈퇴 시 → 이벤트 발행 → Story/Reaction Service가 이벤트 수신 후 관련 데이터 처리

3. **Soft Delete (논리 삭제)**
   - 회원 탈퇴 시 user_status를 'DELETED'로 변경 (물리 삭제하지 않음)
   - Story/Reaction Service는 작성자 정보 조회 시 "탈퇴한 회원" 표시

### 데이터 조회 패턴

#### Pattern 1: N+1 문제를 방지하는 Batch 조회
```java
// 소설 목록의 작성자 정보를 한 번의 API 호출로 조회
List<Long> writerIds = books.stream()
    .map(Book::getWriterId)
    .distinct()
    .collect(Collectors.toList());

ApiResponse<MemberBatchInfoDto> response =
    memberServiceClient.getMembersBatch(writerIds);
Map<Long, MemberInfoDto> memberMap = response.getData().getMembers();
```

#### Pattern 2: Application Level Join
```java
// Story Service + Member Service + Reaction Service 데이터 조립
BookDetailDto detail = BookDetailDto.builder()
    .book(book)  // Story Service 데이터
    .writer(memberMap.get(book.getWriterId()))  // Member Service 데이터
    .reactions(reactionClient.getBookReactionStats(bookId))  // Reaction Service 데이터
    .build();
```

---

## 🛠️ 데이터베이스 마이그레이션

### 스키마 변경 프로세스

1. **스크립트 작성**
   - `database-scripts/migrations/` 디렉토리에 버전별 마이그레이션 스크립트 작성
   - 명명 규칙: `V{version}__{description}.sql` (예: `V2.0__add_book_thumbnail.sql`)

2. **로컬 테스트**
   - 개발 환경에서 먼저 테스트 실행

3. **롤백 계획**
   - 각 마이그레이션마다 롤백 스크립트 준비

4. **배포**
   - 운영 환경에 적용 (점진적 배포 권장)

### Flyway/Liquibase 통합 (향후 계획)

현재는 수동 SQL 스크립트를 사용하지만, 향후 Flyway 또는 Liquibase를 도입하여 자동 마이그레이션을 구현할 예정입니다.

---

## 🔍 데이터베이스 모니터링

### 권장 도구
- **슬로우 쿼리 로그**: MariaDB slow_query_log 활성화
- **인덱스 사용률 분석**: `EXPLAIN` 명령어로 쿼리 플랜 확인
- **커넥션 풀 모니터링**: HikariCP 메트릭 확인

### 주요 모니터링 지표
- 커넥션 풀 사용률
- 평균 쿼리 실행 시간
- 테이블 크기 및 인덱스 크기
- 슬로우 쿼리 발생 빈도

---

## ⚡ 성능 최적화 전략

### 1. 인덱스 전략

각 테이블에는 조회 성능을 위한 인덱스가 적용되어 있습니다.

#### Story Service 인덱스

**books 테이블**:
```sql
INDEX idx_writer_id (writer_id)          -- 작성자별 소설 조회
INDEX idx_category (category_id)         -- 카테고리별 조회
INDEX idx_status (status)                -- 상태별 조회 (진행 중/완결)
INDEX idx_created_at (created_at)        -- 최신순 정렬
```

**sentences 테이블**:
```sql
INDEX idx_book_sequence (book_id, sequence_num)  -- 소설별 문장 순서 조회 (Composite Index)
INDEX idx_writer_id (writer_id)                  -- 작성자별 문장 조회
```

**인덱스 활용 쿼리 예시**:
```sql
-- ✅ 인덱스 사용 (idx_category + idx_status)
SELECT * FROM books
WHERE category_id = 'FANTASY'
  AND status = 'ONGOING'
ORDER BY created_at DESC
LIMIT 10;

-- ✅ Covering Index (idx_book_sequence가 모든 컬럼 커버)
SELECT sequence_num, content
FROM sentences
WHERE book_id = 1
ORDER BY sequence_num;
```

#### Member Service 인덱스

```sql
INDEX idx_email (user_email)             -- 로그인 시 이메일 검색
INDEX idx_nickname (user_nicknm)         -- 닉네임 중복 체크
INDEX idx_status (user_status)           -- 상태별 조회 (관리자 승인 대기 목록 등)
```

#### Reaction Service 인덱스

**comments 테이블**:
```sql
INDEX idx_book_id (book_id)              -- 소설별 댓글 조회
INDEX idx_parent_id (parent_id)          -- 대댓글 조회
INDEX idx_writer_id (writer_id)          -- 작성자별 댓글 조회
```

**book_votes / sentence_votes 테이블**:
```sql
UNIQUE INDEX idx_book_voter (book_id, voter_id)       -- 중복 투표 방지 + 조회 최적화
INDEX idx_book_id (book_id)                           -- 소설별 투표 집계
```

**복합 인덱스 최적화**:
```sql
-- ✅ UNIQUE INDEX가 조회에도 사용됨
SELECT vote_type FROM book_votes
WHERE book_id = 1 AND voter_id = 5;  -- idx_book_voter 사용

-- ✅ 소설별 투표 수 집계
SELECT vote_type, COUNT(*)
FROM book_votes
WHERE book_id = 1
GROUP BY vote_type;  -- idx_book_id 사용
```

---

### 2. 쿼리 최적화 팁

#### 2-1. Batch 조회로 N+1 문제 해결

**AS-IS (N+1 문제)**:
```sql
-- 소설 10개 조회 (1회)
SELECT * FROM books LIMIT 10;

-- 각 소설의 작성자 조회 (10회 - N+1!)
SELECT * FROM users WHERE user_id = 1;
SELECT * FROM users WHERE user_id = 2;
...
```

**TO-BE (Batch 조회)**:
```sql
-- 소설 10개 조회 (1회)
SELECT * FROM books LIMIT 10;

-- 작성자 정보 한 번에 조회 (1회)
SELECT * FROM users WHERE user_id IN (1, 2, 3, 5, 7);  -- Batch API
```

#### 2-2. COUNT(*) 최적화

**대량 데이터 카운트 시**:
```sql
-- ❌ 느림 (Full Table Scan)
SELECT COUNT(*) FROM sentences WHERE book_id = 1;

-- ✅ 빠름 (books 테이블의 current_sequence 컬럼 활용)
SELECT current_sequence FROM books WHERE book_id = 1;
```

#### 2-3. LIMIT 쿼리 최적화

**페이징 조회 시**:
```sql
-- ✅ created_at 인덱스 사용
SELECT * FROM books
WHERE status = 'ONGOING'
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;

-- ✅ Keyset Pagination (더 효율적)
SELECT * FROM books
WHERE status = 'ONGOING'
  AND created_at < '2026-01-15 10:00:00'  -- 마지막 조회 시간
ORDER BY created_at DESC
LIMIT 10;
```

---

### 3. 커넥션 풀 설정 (HikariCP)

**권장 설정** (`application.yml`):
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10         # 최대 커넥션 수 (서비스별 조정)
      minimum-idle: 5               # 최소 유휴 커넥션
      connection-timeout: 30000     # 커넥션 대기 시간 (30초)
      idle-timeout: 600000          # 유휴 커넥션 타임아웃 (10분)
      max-lifetime: 1800000         # 커넥션 최대 생명 주기 (30분)
```

**계산 공식**:
```
maximum-pool-size = (core_count × 2) + effective_spindle_count
```

예시:
- CPU 4코어, SSD(spindle=1): 최대 풀 크기 = 4 × 2 + 1 = 9 → **10으로 설정**

---

### 4. 트랜잭션 최적화

#### 4-1. 읽기 전용 트랜잭션

조회 API는 `@Transactional(readOnly = true)` 사용:
```java
@Transactional(readOnly = true)
public List<BookDto> getBookList() {
    // DB 읽기 최적화 (Dirty Checking 비활성화)
}
```

#### 4-2. 트랜잭션 격리 수준

**기본 설정** (READ_COMMITTED):
```yaml
spring:
  jpa:
    properties:
      hibernate:
        connection:
          isolation: 2  # READ_COMMITTED
```

**격리 수준별 특징**:
- READ_UNCOMMITTED (1): 가장 빠르지만 Dirty Read 발생 가능
- **READ_COMMITTED (2)**: 기본값, 성능과 일관성 균형
- REPEATABLE_READ (4): MySQL/MariaDB 기본값, Phantom Read 방지
- SERIALIZABLE (8): 가장 안전하지만 가장 느림

---

### 5. 데이터베이스 백업 전략

#### 5-1. 정기 백업 (Cron)

**전체 백업** (매일 새벽 2시):
```bash
#!/bin/bash
BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)

# 각 데이터베이스 백업
mysqldump -u swcamp -pswcamp next_page_member > $BACKUP_DIR/member_$DATE.sql
mysqldump -u swcamp -pswcamp next_page_story > $BACKUP_DIR/story_$DATE.sql
mysqldump -u swcamp -pswcamp next_page_reaction > $BACKUP_DIR/reaction_$DATE.sql

# 7일 이상 된 백업 파일 삭제
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
```

#### 5-2. 복구 방법

```bash
# 전체 복구
mysql -u swcamp -pswcamp next_page_member < member_20260116_020000.sql

# 특정 테이블만 복구
mysql -u swcamp -pswcamp next_page_member -e "DROP TABLE users;"
mysql -u swcamp -pswcamp next_page_member < member_20260116_020000.sql --tables users
```

---

## 📊 ERD (Entity Relationship Diagram)

전체 시스템 ERD는 상위 디렉토리의 `README.md` 파일을 참고하세요.

각 서비스별 상세 ERD:
- Member Service: users ↔ refresh_token (1:N)
- Story Service: categories ↔ books (1:N), books ↔ sentences (1:N)
- Reaction Service: comments (Self Join), book_votes, sentence_votes

---

**Last Updated:** 2026-01-16
**Version:** 3.0
