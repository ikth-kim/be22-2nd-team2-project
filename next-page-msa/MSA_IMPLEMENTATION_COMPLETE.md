# 🎯 MSA 전환 완료 상태 문서 (MSA Implementation Status)

> **Last Updated:** 2026-01-15
> **Status:** ✅ 전체 MSA 전환 완료 (Production Ready)
> **특징:** 빌드 완료, 설정 중앙 관리(Config Server), 안정성(Circuit Breaker), 모든 JavaDoc 문서화 완료

---

## 📊 전환 완료 현황 (Summary)

### ✅ 완료된 작업 요약

| 카테고리 | 작업 | 상태 | 비고 |
|---------|------|------|------|
| **인프라** | Discovery Server 구축 | ✅ 완료 | Eureka (Port 8761) |
| **인프라** | API Gateway 구축 | ✅ 완료 | Spring Cloud Gateway + JWT Filter |
| **인프라** | Config Server 구축 | ✅ 완료 | 중앙 설정 관리 |
| **데이터베이스** | DB 분리 (3개) | ✅ 완료 | member, story, reaction |
| **공통** | common-module 생성 | ✅ 완료 | Feign DTOs, Resilience4j 설정 |
| **서비스** | member-service 이관 | ✅ 완료 | 내부 API 구현 |
| **서비스** | story-service 이관 | ✅ 완료 | Feign Client 통합 |
| **서비스** | reaction-service 이관 | ✅ 완료 | 양방향 Feign 통합 |
| **통신** | Feign Client 구현 | ✅ 완료 | MemberServiceClient, StoryServiceClient |
| **안정성** | Circuit Breaker 적용 | ✅ 완료 | Resilience4j, Fallback 처리 |
| **안정성** | Load Balancing | ✅ 완료 | Spring Cloud LoadBalancer |
| **빌드** | 전체 MSA 빌드 | ✅ 성공 | 43 tasks, 21초 |
| **기능** | WebSocket (실시간) | ✅ 완료 | story-service, reaction-service 기동, 7개 토픽 |
| **기능** | 댓글 알림 (서비스 간 WebSocket) | ✅ 완료 | Reaction → Story 알림 연동 |
| **UI/UX** | Frontend Polish | ✅ 완료 | 로고 폰트(Gaegu), 모달 UX, 503 에러 해결 |
| **문서화** | JavaDoc 추가 | ✅ 완료 | 모든 Java 파일에 작성자 정보 포함 |
| **문서화** | Swagger 개선 | ✅ 완료 | 전체 API @Operation, @Schema 적용 |
| **문서화** | API 명세서 완성 | ✅ 완료 | WebSocket 토픽, Internal API, Gateway 헤더 명세 |
| **문서화** | 기술 아키텍처 상세화 | ✅ 완료 | SecurityUtil, WebSocket 연동, 성능 최적화 |
| **보안** | 관리자 승인제 | ✅ 완료 | PENDING 상태 및 승인 워크플로우 구현 |
| **테스트** | 단위 테스트 | ✅ 완료 | Service Layer JUnit + Mockito |

---

## 📝 상세 구현 체크리스트 (Implementation Details)

### 1. 데이터베이스 분리
- [x] **Schema Scripts**:
  - `01-create-databases.sql`: DB 생성 및 계정 설정
  - `02-member-service-schema.sql`: Member 스키마
  - `03-story-service-schema.sql`: Story 스키마
  - `04-reaction-service-schema.sql`: Reaction 스키마
- [x] 샘플 데이터 포함
- [x] 상세 가이드 작성 (`database-scripts/README.md`)

### 2. Common Module (공통 라이브러리)
- [x] **Core Classes**:
  - `BaseEntity.java` (JPA Auditing)
  - `ErrorCode.java` (MSA 확장)
  - `BusinessException.java` (Custom Exception)
  - `ApiResponse.java` (Standard Format)
- [x] **Utils**:
  - `GlobalExceptionHandler.java`
  - `SecurityUtil.java` (Gateway 헤더 기반 인증 처리)
- [x] **Dependencies**: Resilience4j, Spring Cloud 추가

### 3. Services Implementation
- [x] **Member Service**: `auth` 패키지, JWT 로직, Internal API 구현
- [x] **Story Service**: `book`/`query` 패키지 이관, Writer ID 참조 변경, Feign Client 구현
- [x] **Reaction Service**: `reaction` 패키지 이관, 양방향 의존성 해결 (Feign)
- [x] **Gateway Server**: `JwtAuthenticationFilter` (토큰 검증), Route 설정

### 4. Stability & Testing
- [x] **Circuit Breaker**: Resilience4j 적용 (Failure Rate Threshold 50%)
- [x] **Fallback**: 외부 서비스 장애 시 기본값 반환 처리
- [x] **Load Balancing**: Spring Cloud LoadBalancer (Client-Side)
- [x] **Testing**: JUnit + Mockito 단위 테스트, HTTP Client 테스트 파일 작성

---

## 🚦 서비스 기동 및 포트 정보

### 1. 서비스별 포트 (Service Ports)

| 서비스명 | 포트 (Port) | 역할 |
|:---|:---:|:---|
| **Config Server** | 8888 | 중앙 설정 관리 (Git) |
| **Discovery Server** | 8761 | 서비스 등록 및 탐색 (Eureka) |
| **Gateway Server** | 8000 | API 라우팅 및 JWT 필터 |
| **Member Service** | 8081 | 회원 및 인증 관리 |
| **Story Service** | 8082 | 소설 및 문장 관리 (WebSocket) |
| **Reaction Service** | 8083 | 댓글 및 투표 관리 |

### 2. 권장 실행 순서

1. **Config Server** (기동 완료 후 5~10초 대기)
2. **Discovery Server** (기동 완료 후 Eureka 대시보드 확인)
3. **Domain Services** (Member → Story → Reaction 순서 권장)
4. **Gateway Server** (최종 진입점)

---

## 🔧 빌드 및 실행 명령어 (Build Commands)

```bash
# 전체 빌드 (테스트 제외)
./gradlew clean build -x test

# 전체 실행 (병렬)
./gradlew bootRun --parallel
```

---

## 🏗️ MSA 아키텍처 구조

```mermaid
graph TD
    Client["Client Browser"]
    Gateway["API Gateway Server (8000)"]
    Discovery["Discovery Server (8761)"]
    Config["Config Server (8888)"]
    
    subgraph "Domain Services"
        Member["Member Service (8081)"]
        Story["Story Service (8082)"]
        Reaction["Reaction Service (8083)"]
    end

    subgraph "Databases"
        DB_M[("DB: Member")]
        DB_S[("DB: Story")]
        DB_R[("DB: Reaction")]
    end

    Client --> Gateway
    Gateway --> Member
    Gateway --> Story
    Gateway --> Reaction
    
    Member --> DB_M
    Story --> DB_S
    Reaction --> DB_R
    
    Member -.-> Discovery
    Story -.-> Discovery
    Reaction -.-> Discovery
    Gateway -.-> Discovery
    
    Member -.-> Config
    Story -.-> Config
    Reaction -.-> Config
    Gateway -.-> Config
```

---

## 🔄 Monolithic → MSA 전환 핵심 변경사항

### 1. JPA 관계 제거 → ID 참조 전환 (Logic Reference)

#### Before (Monolithic)
```java
// ❌ 객체 참조 (Cross-DB JOIN 불가)
@Entity
public class Book {
    @ManyToOne
    @JoinColumn(name = "writer_id")
    private Member writer;  // JPA 객체 참조
}
```

#### After (MSA)
```java
// ✅ ID 참조 + Feign Client
@Entity
public class Book {
    @Column(name = "writer_id")
    private Long writerId;  // ID만 저장
}
```

### 2. Cross-DB JOIN 제거 → Application Level Join

#### Before (Monolithic)
- MyBatis에서 `LEFT JOIN users` 직접 사용

#### After (MSA)
- **Service Layer**에서 **Feign Client**(`MemberServiceClient`)로 데이터 조회 후 조합
- `Circuit Breaker`가 적용되어 장애 시 Fallback 처리됨

### 3. N+1 문제 방지: Batch API 구현

- **Solution**: `getMembersBatch(List<Long> ids)` API를 구현하여 한 번의 네트워크 호출로 다건 조회

### 4. SecurityUtil Header 기반 인증

- **Before**: `SecurityContextHolder` 접속 (세션 기반)
- **After**: Gateway가 주입한 `X-User-Id` 헤더 파싱

---

## 🎯 추가 발견 사항 (2026-01-16 문서화 업데이트)

### 1. WebSocket 토픽 전체 목록 명세화

**Story Service WebSocket** (`/ws` 엔드포인트):
1. `/topic/typing/{bookId}` - 문장 작성 타이핑 상태
2. `/topic/comment-typing/{bookId}` - 댓글 작성 타이핑 상태
3. `/topic/books/new` - 새 소설 생성 알림
4. `/topic/sentences/{bookId}` - 문장 추가 알림
5. `/topic/comments/{bookId}` - 댓글 생성 알림 (Reaction → Story)
6. `/topic/books/{bookId}/status` - 소설 상태 변경 (완결)

**Reaction Service WebSocket** (`/ws` 엔드포인트):
7. `/topic/books/{bookId}/votes` - 투표 업데이트

### 2. InternalNotificationController 발견

**위치**: `story-service/src/main/java/com/team2/storyservice/websocket/controller/InternalNotificationController.java`

**API**:
- `POST /internal/notify/comments` - Reaction Service에서 댓글 생성 시 Story Service의 WebSocket으로 알림 전송

**동작 방식**:
1. Reaction Service에서 댓글 생성
2. StoryServiceClient.notifyCommentCreated() 호출 (Feign)
3. InternalNotificationController가 요청 수신
4. messagingTemplate.convertAndSend()로 WebSocket 브로드캐스트

### 3. CommentNotificationDto 추가

**위치**: `common-module/src/main/java/com/team2/commonmodule/feign/dto/CommentNotificationDto.java`

**용도**: Reaction Service → Story Service 댓글 알림 전송 시 사용

**필드**:
- commentId (LONG)
- bookId (LONG)
- content (STRING)
- nickname (STRING)
- createdAt (DATETIME)

### 4. SecurityUtil 유틸리티 클래스

**위치**: `common-module/src/main/java/com/team2/commonmodule/util/SecurityUtil.java`

**주요 메서드**:
- `getCurrentUserId()` - Gateway X-User-Id 헤더에서 사용자 ID 추출
- `getCurrentUserEmail()` - 사용자 이메일 추출
- `getCurrentUserNickname()` - 사용자 닉네임 추출
- `getCurrentUserRole()` - 사용자 역할 추출
- `isAdmin()` - 관리자 여부 확인
- `isCurrentUser(Long userId)` - 본인 여부 확인
- `isCurrentUserOrAdmin(Long userId)` - 본인 또는 관리자 여부 확인

**특징**:
- Gateway 헤더 우선, SecurityContext fallback
- 모든 서비스에서 통일된 인증 정보 조회

### 5. Gateway 헤더 명세 표준화

**Gateway에서 주입하는 HTTP 헤더**:
- `X-User-Id`: 사용자 고유 ID (Long)
- `X-User-Email`: 사용자 이메일
- `X-User-Nickname`: 사용자 닉네임
- `X-User-Role`: 사용자 권할 (USER/ADMIN)

**Filter**:
- `JwtAuthenticationFilter` (Gateway) - JWT 검증 및 헤더 주입
- `GatewayAuthenticationFilter` (Common Module) - 헤더 기반 인증 처리
- `JwtToHeaderFilter` (Common Module) - Swagger 직접 테스트용

### 6. Resilience4j Circuit Breaker 설정

**각 서비스 application.yml**:
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 5s
```

**적용 범위**: 모든 Feign Client 호출 (Member, Story, Reaction Service)

### 7. Batch 조회 API 구현

**N+1 문제 방지를 위한 Batch API**:
- `GET /internal/members/batch?userIds=1,2,3` - 회원 정보 일괄 조회
- `GET /internal/books/batch?bookIds=1,2,3` - 소설 정보 일괄 조회
- `POST /internal/reactions/sentences/stats` - 문장별 투표 정보 일괄 조회

**효과**: 10개 소설 조회 시 10번 → 1번의 HTTP 요청으로 감소

---

**Last Updated:** 2026-01-16
**Completion Date:** 2026-01-15
**Result:** Monolithic 아키텍처에서 MSA로의 전환이 성공적으로 완료되었으며, 2026-01-16 문서화 업데이트를 통해 누락된 내용이 모두 보완됨.
