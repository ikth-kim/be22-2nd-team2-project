# 🧑‍💻 Next Page Developer Guide

본 가이드는 **Next Page** 프로젝트의 개발자들이 참고할 수 있는 **API 테스트, 기능 명세, 개발 가이드** 문서입니다.  
각 담당자는 본인이 개발한 파트에 대한 설명을 참고하고, 새로운 기능 개발 시 이 문서를 업데이트해 주세요.

---

## 📑 Table of Contents (목차)
1.  [Coding Conventions & Patterns](#1-%EF%B8%8F-coding-conventions--patterns)
2.  [Getting Started](#2--getting-started)
3.  [API Testing Guide](#3--api-testing-guide)
4.  [Module-Specific Guide](#4-%EF%B8%8F-module-specific-guide-담당자별-가이드)
5.  [Real-time System Flow](#5--real-time-system-flow)
6.  [Migration to MSA Guide](#6--migration-to-msa-microservices-guide)
7.  [Testing Strategy](#7--testing-strategy)
8.  [Troubleshooting](#8-%EF%B8%8F-troubleshooting)

---

## 1. 🏛️ Coding Conventions & Patterns

### 1-1. CQRS & Architecture
*   **Command:** 데이터 상태를 변경하는 모든 로직은 `command` 패키지에서 **JPA**를 사용해 처리합니다.
    *   Entity의 비즈니스 메서드를 통해 상태를 변경합니다 (Setter 지양).
*   **Query:** 데이터 조회가 주 목적(화면 표시 등)인 로직은 `query` 패키지에서 **MyBatis**를 사용합니다.
    *   복잡한 조인, 통계, DTO 매핑 최적화에 유리합니다.

### 1-2. WebSocket Convention
*   **Prefix:** 클라이언트 요청은 `/app`, 서버 브로드캐스팅은 `/topic`을 사용합니다.
*   **Payload:** 모든 실시간 메시지는 JSON 포맷으로 주고받습니다.

### 1-3. Security & Utils
*   `SecurityUtil.getCurrentUserId()`를 통해 언제든 안전하게 현재 사용자 ID를 획득할 수 있습니다.
*   하드코딩된 ID 대신 반드시 동적 ID를 사용하세요.

---

## 2. 🚀 Getting Started

### 1-1. 실행 환경
*   **JDK 17 +**
*   **MariaDB 10.6 +**
*   **Redis** (Optional, if configured)

### 1-2. 실행 방법
```bash
./gradlew bootRun
```
서버가 정상적으로 실행되면 `http://localhost:8080`으로 접속하여 테스트할 수 있습니다.

---

## 2. 🔌 API Testing Guide

개발 중인 API를 테스트하는 방법은 크게 두 가지가 있습니다.

### 2-1. Swagger UI 활용 (추천 ✅)
브라우저에서 API 명세를 확인하고 즉시 요청을 보낼 수 있습니다.
*   **URL:** [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)
*   **기능:** API Endpoint 확인, Try it out 기능을 통한 요청/응답 테스트.

### 2-2. IntelliJ HTTP Client 활용
`http` 디렉토리 내에 있는 `.http` 파일을 통해 시나리오 테스트를 수행할 수 있습니다.
*   **파일 위치:** `next-page/http/api-test.http`
*   **사용법:**
    1. IntelliJ에서 해당 파일을 엽니다.
    2. `Run` 버튼(▶️)을 클릭하여 요청을 전송합니다.
    3. `global.json` 또는 환경 변수를 통해 JWT 토큰을 관리하며 연속적인 시나리오 테스트가 가능합니다.

---

## 3. 🏗️ Module-Specific Guide (담당자별 가이드)

### 👤 Member & Auth (김태형)
*   **보안 설정 (`SecurityConfig`):** URL 별 권한 설정이 필요할 때 수정하세요.
*   **JWT:** `JwtTokenProvider`에서 토큰 생성/검증 로직을 관리합니다.
*   **Soft Delete:** 회원 탈퇴 시 `DELETE` 쿼리 대신 `status = 'DELETED'` 업데이트를 수행합니다.
*   **트랜잭션:** 회원 상태 변경 등 중요 로직에는 반드시 `@Transactional`을 적용하세요.

### 📖 Book & Writing (정진호)
*   **동시성 제어:** 여러 명이 동시에 문장을 입력할 때의 충돌 방지가 핵심입니다.
    *   `last_writer_user_id`를 체크하여 연속 작성을 방지합니다 (`Book` 엔티티 참조).
    *   `SEQUENCE_MISMATCH` 예외를 통해 오직 **마지막 문장**만 수정/삭제할 수 있도록 제한합니다. (과거 기록 보호)
*   **편집 잠금 (Locking):** 문장을 편집하는 동안 `TypingController`를 통해 수정 중임을 알리고 다른 유저의 입력을 차단합니다.
*   **WebSocket:** `TypingController`에서 실시간 입력 상태를 브로드캐스팅합니다.
    *   Topic: `/topic/typing/{bookId}` (Book ID별 격리 필수)

### ❤️ Reaction & Comment (정병진)
*   **실시간 댓글 (Real-time):** 댓글 등록 시 `ReactionController`에서 `SimpMessagingTemplate`를 사용해 `/topic/comments/{bookId}`로 이벤트를 발행합니다.
    *   프론트엔드는 이 토픽을 구독하여 새로고침 없이 댓글을 즉시 리스트에 추가합니다.
*   **계층형 댓글:** `parent_id`를 활용한 자기참조 구조입니다.
    *   조회 시 계층 구조로 변환하는 로직(`ReactionQueryService`)을 주의 깊게 다루세요.
*   **투표 (Vote):** 1인 1투표 원칙을 `uk_book_voter`, `uk_sentence_voter` 유니크 키로 보장합니다.

---

## 4. 🌐 Real-time System Flow

### 4-1. WebSocket & STOMP Flow
본 프로젝트는 **Spring WebSocket + STOMP**를 사용하여 실시간 양방향 통신을 구현합니다.

1.  **Frontend Connect:** `new SockJS('/ws')` -> `Stomp.over(socket)` -> `.connect()`
2.  **Subscribe:**
    *   `/topic/typing/{bookId}`: 타이핑 상태 감지 (누가 쓰고 있는지)
    *   `/topic/comments/{bookId}`: 새 댓글 감지
    *   `/topic/sentences/{bookId}`: 새 문장(이어쓰기) 감지
3.  **Publish (Frontend):** 타이핑 시작/종료 시 `/app/typing` 등으로 메시지 전송.
4.  **Publish (Backend Events):** 댓글/문장 작성 완료 시 컨트롤러에서 `convertAndSend`로 이벤트 브로드캐스팅.

---

## 5. 🚀 MSA(Microservices Architecture) 전환 상세 가이드

이 섹션은 현재의 Monolithic 아키텍처를 MSA 환경으로 안전하고 완전하게 전환하기 위한 **단계별 통합 매뉴얼**입니다. 도메인 분리와 서비스 간 통신 설정을 포함하여 100% 전환이 가능하도록 가이드합니다.

### 5-1. 목표 아키텍처 (Target Architecture)

```text
                  [Service Discovery: Eureka]
                           |
            [Gateway Server: Spring Cloud Gateway]  <-- (Port 8000)
                           |
    -------------------------------------------------------
    |                |                  |                 |
[Member Service] [Story Service] [Reaction Service] [Config Server]
(Auth/User)      (Book/Sentence) (Comment/Vote)     (Central Config)
    |                |                  |
 [DB: User]       [DB: Story]       [DB: Reaction]
```

### 5-2. 사전 준비 (Workspace Setup)
1.  기존 `next-page` 폴더와 병렬 위치에 `next-page-msa` 폴더를 생성합니다.
2.  루트 프로젝트(`next-page-msa`)를 IntelliJ에서 Gradle 프로젝트로 생성하고 **`src` 폴더를 즉시 삭제**합니다.

---

### 5-3. [Module 1] `common-module` (공통 라이브러리)
모든 서비스가 의존하는 핵심 유틸리티 모듈입니다. 다른 서비스보다 **가장 먼저** 구축해야 합니다.

*   **생성 위치:** `next-page-msa/common-module`
*   **이동 대상 코드 (From `next-page`):**
    *   `src/main/java/com/team2/nextpage/common/**` (전체 복사)
    *   `src/main/java/com/team2/nextpage/config/ModelMapperConfig.java`
*   **핵심 역할:** `ApiResponse`, `BusinessException`, `ErrorCode`, `SecurityUtil` 등 도메인 전반에서 공통으로 사용되는 로직 공유.
*   **비고:** `build.gradle`에 `java-library` 플러그인을 적용하여 각 마이크로서비스 모듈에서 라이브러리 형태로 참조합니다.

---

### 5-4. [Module 2] `discovery-server` (인프라 서비스)
*   **Dependencies:** `spring-cloud-starter-netflix-eureka-server`
*   **Main Class:** `DiscoveryApplication.java` (Annotation: `@EnableEurekaServer`)
*   **YML 설정:** 포트 8761 고정 및 서버 자체 등록 비활성화.

---

### 5-5. [Module 3] `gateway-server` (진입점 및 보안)
*   **Dependencies:** `gateway`, `eureka-client`, `jjwt`
*   **주요 구현 사항:**
    *   `src/main/java/com/team2/nextpage/jwt/**` 로직을 기반으로 **`JwtAuthenticationFilter`를 Gateway 전용 GlobalFilter로 이관**합니다.
    *   토큰 검증 성공 시, 내부 마이크로서비스로 전달할 헤더(`X-User-Id`, `X-User-Email`)를 주입합니다.

---

### 5-6. [Domain Services] 마이크로서비스별 이관 매뉴얼
도메인 분리 시 **패키지 경로(`com.team2.nextpage...`)를 동일하게 유지**하여 코드 수정 범위를 최소화하는 것을 권장합니다.

#### 📦 [member-service] (Port: 8081)
*   **이관 대상 파일:**
    *   `package com.team2.nextpage.auth/**` (인증 로직)
    *   `package com.team2.nextpage.command.member/**` (회원 명령)
    *   `package com.team2.nextpage.query.member/**` (회원 조회)
    *   `src/main/resources/mapper/member/**.xml`
*   **DB 구성:** `next_page_member` 스키마 전담 관리.

#### 📦 [story-service] (Port: 8082)
*   **이관 대상 파일:**
    *   `package com.team2.nextpage.command.book/**`
    *   `package com.team2.nextpage.query.book/**`
    *   `package com.team2.nextpage.category/**` (카테고리 도메인)
    *   `package com.team2.nextpage.websocket/**` (실시간 기능)
    *   `src/main/java/com/team2/nextpage/config/WebSocketConfig.java`
    *   `src/main/resources/mapper/book/**.xml` 및 `category/**.xml`
*   **수정 사항:** `Book` 엔티티와 `Member` 사이의 객체 참조를 **ID 참조(`Long memberId`)** 방식으로 변경합니다.

#### 📦 [reaction-service] (Port: 8083)
*   **이관 대상 파일:**
    *   `package com.team2.nextpage.command.reaction/**`
    *   `package com.team2.nextpage.query.reaction/**`
    *   `src/main/resources/mapper/reaction/**.xml`
*   **설계 참고:** 다른 서비스와의 결합도가 높은 경우 `Kafka` 등 메시지 브로커를 통한 비동기 데이터 처리를 권장합니다.

---

### 5-7. 마이크로서비스 전환을 위한 필수 수정 사항

1.  **데이터베이스 조인 처리:** 마이바티스 매퍼에서 서비스 간 단절된 테이블을 직접 Join 하는 쿼리는 제거합니다. 대신 **Service 레이어에서 OpenFeign을 통해 데이터를 조회한 후 병합**합니다.
2.  **보안 컨텍스트 활용:** 각 서비스는 인프라(Gateway)에서 전달받은 헤더 값을 읽어 사용자 정보를 획득하도록 `SecurityUtil`을 수정합니다.
3.  **트랜잭션 관리:** 분산 환경에서 일관성이 중요한 로직은 **SAGA 패턴** 도입을 고려하거나, Feign을 이용한 엄격한 검증(Strict Validation) 로직을 선행합니다.
### 5-8. 모듈별 `application.yml` 풀 코드 (Copy & Paste)

각 서비스 모듈의 `src/main/resources/application.yml`에 그대로 복사하여 사용할 수 있는 완전한 설정 코드입니다.

#### 🛰️ Discovery Server (인프라)
```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false # 서버 자신은 등록하지 않음
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

#### ⛩️ Gateway Server (인증 및 라우팅)
```yaml
server:
  port: 8000

spring:
  application:
    name: gateway-server
  cloud:
    gateway:
      routes:
        # Member Service 라우팅
        - id: member-service
          uri: lb://MEMBER-SERVICE
          predicates:
            - Path=/api/auth/**, /api/members/**
        
        # Story Service 라우팅
        - id: story-service
          uri: lb://STORY-SERVICE
          predicates:
            - Path=/api/books/**, /ws/**
        
        # Reaction Service 라우팅
        - id: reaction-service
          uri: lb://REACTION-SERVICE
          predicates:
            - Path=/api/reactions/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

# JWT Secret (기존 SecurityUtil과 동일해야 함)
jwt:
  secret: your_very_long_and_secure_secret_key_here
```

#### 📦 Member Service (회원/인증)
```yaml
server:
  port: 8081

spring:
  application:
    name: MEMBER-SERVICE
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3306/next_page_member
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

mybatis:
  mapper-locations: classpath:mapper/member/**/*.xml
  configuration:
    map-underscore-to-camel-case: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### 📦 Story Service (소설/WebSocket)
```yaml
server:
  port: 8082

spring:
  application:
    name: STORY-SERVICE
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3306/next_page_story
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

mybatis:
  mapper-locations: classpath:mapper/book/**/*.xml, mapper/category/**/*.xml
  configuration:
    map-underscore-to-camel-case: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

# WebSocket은 Gateway(8000)를 통해서도 접속 가능하도록 설정
```


### 5-4. Common Configuration (공통 설정)
*   **Shared Library:** 모든 서비스에서 공통으로 쓰는 DTO(`ApiResponse`, `ErrorCode`)나 Util(`SecurityUtil`)은 별도의 `common-module`로 분리하여 각 서비스가 의존하게 만드는 것이 좋습니다.
*   **JWT Sharing:** Gateway에서 토큰을 검증하고 헤더에 `X-User-Id`를 담아 넘기거나, 각 서비스가 동일한 Secret Key를 공유하여 토큰을 파싱해야 합니다. (Gateway 검증 방식 추천)

---

## 6. 🧪 Testing Strategy

### 6-1. Unit Test (단위 테스트)
*   **Domain Logic:** 비즈니스 로직(엔티티 메서드 등)은 반드시 단위 테스트를 작성합니다.
*   **Service Layer:** Mockito를 활용하여 의존성을 격리하고 테스트하세요.

### 6-2. Integration Test (통합 테스트)
*   DB 연동이 필요한 쿼리나 전체 흐름 테스트는 `@SpringBootTest`를 활용합니다.
*   실제 `http/api-test.http`를 사용하여 End-to-End 시나리오를 점검하는 것을 권장합니다.

---

## 7. ⚠️ Troubleshooting
*   **인코딩 문제:** `application.yml`의 인코딩 설정(`UTF-8`)을 확인하세요.
*   **WebSocket 연결 실패:** 클라이언트의 `SockJS` 버전과 서버 설정을 확인하고, Security Config에서 `/ws/**` 경로가 허용되어 있는지 체크하세요.

---
**Last Updated:** 2026-01-14
