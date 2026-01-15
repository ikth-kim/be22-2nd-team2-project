<div align="center"><img src="images/logo.png" width="300" alt="Next Page Logo"></div>

# 📚 Next Page : 우리가 함께 만드는 실시간 릴레이 소설
>
> **"당신의 한 문장이 베스트셀러의 시작이 됩니다."**
> 누구나 작가가 되어 실시간으로 소설을 완성하는 집단 지성 창작 플랫폼

**문서 구조:**

- **PART 1: 공통 (Common)** - 프로젝트 개요, 팀 구성, 기술 스택, 기능 명세
- **PART 2: AS-IS (Monolithic)** - 초기 단일 애플리케이션 아키텍처 (2025.12.23 ~ 2026.01.10)
- **PART 3: TO-BE (MSA)** - Microservices Architecture (2026.01.11 ~ 현재) ✅ 현재 운영

<br>

## 📑 목차

### PART 1: 공통 (Common)

1. [프로젝트 소개](#part-1-1-프로젝트-소개)
2. [팀원 및 역할 분담](#part-1-2-팀원-및-역할-분담)
3. [기술 스택](#part-1-3-기술-스택)
4. [주요 기능 & 유스케이스](#part-1-4-주요-기능--유스케이스)
5. [요구사항](#part-1-5-요구사항-정의서)
6. [ERD 설계](#part-1-6-erd-설계)
7. [Database Schema](#part-1-7-database-schema)
8. [팀 그라운드 룰 (XP)](#part-1-8-팀-그라운드-룰-xp)

### PART 2: AS-IS (Monolithic)

1. [Monolithic 아키텍처](#part-2-1-monolithic-아키텍처)
2. [Monolithic 실행 방법](#part-2-2-monolithic-실행-방법)
3. [Monolithic 패키지 구조](#part-2-3-monolithic-패키지-구조)

### PART 3: TO-BE (MSA) ✅

1. [MSA 아키텍처](#part-3-1-msa-아키텍처)
2. [MSA 시스템 구성도](#part-3-2-msa-시스템-구성도)
3. [MSA 전환 핵심 포인트](#part-3-3-msa-전환-핵심-포인트)
4. [MSA 시퀀스 다이어그램](#part-3-4-msa-시퀀스-다이어그램)
5. [실행 방법](#part-3-5-실행-방법)

<br>

---

# PART 1: 공통 (Common)

프로젝트 전반에 대한 개요, 팀 구성, 기술 스택, 기능 요구사항을 다룹니다.

---

## PART 1-1. 프로젝트 소개

**Next Page**는 한 사람이 모든 이야기를 쓰는 것이 아니라, 여러 사용자가 **문장 단위로 이어 쓰며 하나의 소설을 완성**하는 릴레이 창작 서비스입니다.

단순한 게시판이 아닙니다. **WebSocket을 활용한 실시간 타이핑 표시**, **순서(Sequence) 제어**, **투표 기반 평가**가 결합된 몰입형 창작 플랫폼입니다. 앞사람이 글을 완료해야만 뒷사람이 쓸 수 있는 **동시성 제어**와 **도메인 규칙**을 엄격하게 준수합니다.

### 📅 개발 기간

- **2025.12.23 ~ 2026.01.16** (총 4주)

### 🏗️ 아키텍처 변천

- **Phase 1 - Monolithic (2025.12.23 ~ 2026.01.10):** 단일 Spring Boot 애플리케이션
- **Phase 2 - MSA (2026.01.11 ~ 현재):** Microservices Architecture로 전환 완료 ✅

### 📊 프로젝트 진행 현황

| 기능 영역 | 상태 | 완료 항목 |
|:---:|:---:|:---|
| **⚡ 실시간/웹소켓** | ✅ 완료 | **실시간 타이핑/편집 잠금**, **댓글/문장 실시간 푸시**, STOMP 프로토콜 최적화 |
| **🔐 인증/인가** | ✅ 완료 | JWT, Refresh Token, 강제 로그인 모달, **Soft Delete(User)** |
| **👤 회원 관리** | ✅ 완료 | 회원가입/탈퇴, **실시간 입력값 검증**, 마이페이지 대시보드 |
| **📖 소설 집필** | ✅ 완료 | **문장 이어쓰기(순서 제어)**, **마지막 문장만 수정/삭제(Restriction)**, **편집 중 잠금(Lock)** |
| **📚 소설 조회** | ✅ 완료 | 무한 스크롤, 페이징/정렬, **책 넘김 효과 뷰어**, **내가 쓴 글/댓글 모아보기** |
| **❤️ 반응/평가** | ✅ 완료 | **실시간 댓글(WebSocket)**, 계층형 대댓글, 개추/비추 투표, Soft Delete(Comment) |
| **🏗️ MSA 전환** | ✅ 완료 | **Eureka**, **Gateway + JWT Filter**, **Feign Client**, 3개 DB 분리, **Application Level Join** |
| **🛡️ 안정성** | ✅ 완료 | **Resilience4j (Circuit Breaker)** 적용, 장애 전파 방지 및 Fallback 처리 |
| **API & Architecture** | ✅ 완료 | **HATEOAS**, CQRS, Swagger, **Dynamic Topic Routing (/topic/{bookId})** |
| **🎨 UI/UX** | ✅ 완료 | **Cute Pop 디자인**, 로고 폰트(Gaegu), 모달 UX 개선, 토스트 알림 |
| **🧪 테스트** | ✅ 완료 | 전체 API 시나리오 테스트 (`http/api-test.http`), 단위/통합 테스트 환경 |

<br>

---

## PART 1-2. 팀원 및 역할 분담

우리는 **도메인 주도 설계(DDD)** 원칙에 따라, 기능 단위가 아닌 **도메인(Context)** 단위로 역할을 분담하여 전문성을 높였습니다.

| 이름 | 포지션 | 담당 도메인 & 핵심 역할 |
|:---:|:---:|:---|
| **정진호** | **Team Leader** | **🏛 Core & Architecture**<br>- 프로젝트 아키텍처 설계 (CQRS, WebSocket, MSA 전환)<br>- 실시간 타이핑 및 알림 시스템 구현<br>- `Story` 애그리거트 상태/순서 제어 로직<br>**✍️ Writing & Query**<br>- 문장 작성(Append) 및 유효성 검사<br>- 동적 쿼리(MyBatis) 기반 조회/검색 최적화 |
| **김태형** | **Sub Leader** | **🔐 Member & Auth**<br>- Spring Security + JWT 인증/인가 시스템<br>- **Soft Delete**를 적용한 안전한 회원 탈퇴/관리<br>- 실시간 유효성 검증 로직 및 마이페이지<br>- MSA: member-service 구축 및 Internal API 제공 |
| **정병진** | **Developer** | **❤️ Reaction & Support**<br>- **개추/비추(Thumb Up/Down)** 투표 시스템<br>- 계층형 댓글(대댓글) 구조 설계 및 구현<br>- 관리자 권한(댓글/유저 관리) 기능 구현<br>- MSA: reaction-service 구축 및 양방향 Feign 통합 |
| **최현지** | **Document Manager** | **📄 Documentation & QA**<br>- 프로젝트 문서화 및 리드미(README) 관리<br>- API 명세 및 개발자 가이드 최신화<br>- 전체 기능 QA 및 시나리오 점검 |

<br>

---

## PART 1-3. 기술 스택

### 💻 개발 환경

- **IDE:** IntelliJ IDEA
- **JDK:** Java 17 (Amazon Corretto or Azul Zulu)
- **Database:** MariaDB 10.6+
- **Build Tool:** Gradle
- **Framework:** Spring Boot 3.5.9

### Backend & Real-time

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.1-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Eureka](https://img.shields.io/badge/Eureka-Service%20Discovery-6DB33F?style=flat-square)
![Gateway](https://img.shields.io/badge/Gateway-Spring%20Cloud-6DB33F?style=flat-square)
![Feign](https://img.shields.io/badge/Feign-OpenFeign-6DB33F?style=flat-square)
![Resilience4j](https://img.shields.io/badge/Resilience4j-Circuit%20Breaker-6DB33F?style=flat-square)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-000000?style=flat-square&logo=websocket&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?style=flat-square&logo=hibernate&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-3.5-C63B2B?style=flat-square&logo=mybatis&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-10.6-003545?style=flat-square&logo=mariadb&logoColor=white)

### Frontend

![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white)
![jQuery](https://img.shields.io/badge/jQuery-3.6-0769AD?style=flat-square&logo=jquery&logoColor=white)
![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D?style=flat-square&logo=vue.js&logoColor=white)
![SockJS](https://img.shields.io/badge/SockJS-Realtime-000000?style=flat-square)

### Architecture Strategy (공통 패턴)

- **RESTful API:** 자원(Resource) 중심의 명확한 URI 설계 및 HTTP Method 활용
- **CQRS Pattern:**
  - **Command (쓰기):** JPA (Domain Logic, Dirty Checking) - 데이터 무결성 보장
  - **Query (읽기):** MyBatis (Dynamic Query) - 복잡한 통계/조회 성능 최적화
- **Event-Driven:** WebSocket을 통한 실시간 상태 동기화 (작성 중, 댓글 작성 등)

<br>

---

## PART 1-4. 주요 기능 & 유스케이스

사용자와 관리자가 시스템에서 수행할 수 있는 주요 시나리오입니다.

### 🗺️ Use Case Diagram

```mermaid
flowchart TD
    User((일반 회원))
    Guest((게스트))
    Admin((관리자))

    subgraph "👤 Member Domain"
        Auth[회원가입/로그인]
        Withdraw[회원 탈퇴]
        MyPage[마이페이지]
        ViewActivity[활동 내역 조회]
    end

    subgraph "📖 Story Domain - 창작"
        CreateBook[소설방 생성]
        AppendSentence[문장 이어쓰기]
        EditSentence[마지막 문장 수정]
        CompleteBook[소설 수동 완결]
    end

    subgraph "📚 Story Domain - 감상"
        BrowseBooks[소설 목록 조회]
        ReadBook[소설 상세 보기]
        ViewerMode[완결 소설 뷰어 모드]
    end

    subgraph "❤️ Reaction Domain"
        WriteComment[댓글 작성]
        WriteReply[대댓글 작성]
        VoteBook[소설 투표]
        VoteSentence[문장 투표]
    end

    subgraph "⚡ Realtime Features"
        TypingIndicator[타이핑 인디케이터]
        LiveUpdate[실시간 업데이트]
    end

    %% Guest Interactions
    Guest --> BrowseBooks
    Guest --> ReadBook
    Guest --> ViewerMode
    Guest -.->|require login| Auth

    %% User Interactions - Member
    User --> Auth
    User --> Withdraw
    User --> MyPage
    User --> ViewActivity

    %% User Interactions - Story
    User --> CreateBook
    User --> AppendSentence
    User --> EditSentence
    User --> CompleteBook
    User --> BrowseBooks
    User --> ReadBook
    User --> ViewerMode

    %% User Interactions - Reaction
    User --> WriteComment
    User --> WriteReply
    User --> VoteBook
    User --> VoteSentence

    %% User Interactions - Realtime
    User <-.-> TypingIndicator
    User <-.-> LiveUpdate

    %% Admin Interactions
    Admin --> Auth
    Admin -.->|manage| User
    Admin -.->|moderate| WriteComment
    Admin -.->|delete| CreateBook

    %% Relationships
    CreateBook --> AppendSentence
    AppendSentence --> EditSentence
    AppendSentence --> CompleteBook
    ReadBook --> WriteComment
    WriteComment --> WriteReply
    ReadBook --> VoteSentence
    BrowseBooks --> VoteBook
```

### ⚡ 1. 실시간 인터랙션 (WebSocket)

- **실시간 타이핑 인디케이터:** 
  - 누군가 문장을 작성 중이면 다른 사용자들에게 "홍길동님이 문장을 쓰고 있어요..." 표시
  - 댓글 작성 중에도 동일하게 작동
  - 작성 중일 때 다른 사람의 입력 차단 (동시성 제어)
  
- **라이브 업데이트:** 
  - 새 소설이 생성되면 메인 페이지에 즉시 표시
  - 문장이 추가되면 읽고 있는 모든 사용자에게 실시간 반영
  - 댓글이 달리면 즉시 업데이트

### 🚀 2. 릴레이 소설 창작 시나리오

**전체 Flow:**
1. **소설방 개설** (작가A)
   - 제목, 장르, 최대 문장 수 설정
   - 첫 문장 작성 (예: "어느 날, 하늘에서 이상한 물체가 떨어졌다.")

2. **문장 이어쓰기** (작가B, C, D...)
   - 이전 문장을 읽고 다음 스토리 전개
   - **제약 조건:**
     - 직전 작성자는 연속으로 쓸 수 없음 (최소 1명 대기)
     - 현재 순서(sequence)만 작성 가능
     - 실시간 타이핑 중에는 다른 사람 입력 차단

3. **수정 규칙**
   - **마지막 문장만** 수정/삭제 가능 (스토리 일관성 유지)
   - 중간 문장 수정 불가 → 완결 후 전체 다운로드하여 2차 창작 가능

4. **완결**
   - 자동 완결: 최대 문장 수 도달 시
   - 수동 완결: 방장이 임의로 완결 처리
   - 완결 후 뷰어 모드로 전환

### ❤️ 3. 평가 및 소통

- **계층형 댓글:** 
  - 부모 댓글과 대댓글 구조 (depth 제한 없음)
  - Soft Delete로 삭제된 댓글도 "[삭제된 댓글입니다]"로 구조 유지
  
- **투표 시스템:**
  - 소설 전체 평가: 좋아요(개추) / 싫어요(비추)
  - 문장별 평가: 베스트 문장 선정
  - 1인 1투표, 토글 방식 (취소 가능)

### 🎯 4. 사용자 여정 (User Journey)

**신규 사용자 (게스트) → 활발한 작가**
1. 소설 목록 둘러보기 (비로그인 가능)
2. 재미있는 소설 발견 → 회원가입 모달 표시
3. 회원가입 (이메일 중복 체크, 비밀번호 강도 검증)
4. 로그인 후 자동으로 소설 상세 페이지 이동
5. 문장 이어쓰기 → 실시간 타이핑 경험
6. 마이페이지에서 내 활동 확인
7. 새 소설방 개설 → 커뮤니티 확장

<br>

---

## PART 1-5. 요구사항 정의서

### 📋 기능 요구사항 (Functional Requirements)

| 클래스 | ID | 대분류 | 요구사항 명 | 상세 내용 | 비고 |
|:---:|:---:|:---:|:---:|:---|:---|
| **Member** | **FR-101** | 회원 | 회원가입 | 이메일, 비밀번호, 닉네임 입력 및 유효성 검사 (이메일 형식, 비밀번호 복잡도) | 실시간 중복체크 |
| **Member** | **FR-102** | 회원 | 로그인 | 이메일/비밀번호 기반 인증 및 JWT Access Token 발급 | 유효기간 1시간 |
| **Member** | **FR-103** | 회원 | 자동 로그인 | Refresh Token(Cookie)을 이용한 Access Token 재발급 (Silent Refresh) | 유효기간 7일 |
| **Story** | **FR-201** | 소설 | 소설 생성 | 제목, 카테고리(장르), 최대 문장 수 설정을 통한 방 개설 | - |
| **Story** | **FR-204** | 소설 | 문장 이어쓰기 | 현재 순서(Sequence)에 맞춰 새로운 문장 등록 (이전 작성자 작성 불가) | 길이 제한 검증 |
| **Reaction** | **FR-301** | 반응 | 댓글 작성 | 특정 소설에 대한 의견(댓글) 등록 | - |
| **Realtime** | **FR-401** | 실시간 | 타이핑 알림 | 특정 유저가 문장을 작성 중일 때 실시간으로 "OOO님이 작성 중..." 표시 | WebSocket |

### 🛡️ 비기능 요구사항 (Non-Functional Requirements)

| ID | 분류 | 요구사항 명 | 상세 내용 |
|:---:|:---:|:---:|:---|
| **NFR-101** | **Security** | 비밀번호 암호화 | 사용자의 비밀번호는 BCrypt 등 단방향 해시 함수로 암호화하여 저장해야 한다. |
| **NFR-301** | **Architecture** | 서비스 독립성 | MSA 환경에서 한 서비스가 다운되어도 핵심 서비스는 정상 동작해야 한다. |
| **NFR-501** | **Standard** | API 명세 | 모든 API는 Swagger(OpenAPI 3.0)를 통해 문서화되고 테스트 가능해야 한다. |

<br>

---

## PART 1-6. ERD 설계

### 도메인 엔티티 관계 (MSA Version)

MSA 아키텍처에 맞춰 3개의 데이터베이스로 분리된 구조를 반영했습니다. 서비스 간 관계는 물리적 FK가 아닌 **논리적 참조(Logical Reference)**로 연결됩니다.

```mermaid
erDiagram
    %% Member Service
    users {
        BIGINT user_id PK
        VARCHAR email
        VARCHAR nickname
    }

    %% Story Service
    books {
        BIGINT book_id PK
        BIGINT writer_id
        VARCHAR title
    }
    sentences {
        BIGINT sentence_id PK
        BIGINT book_id FK
        BIGINT writer_id
        TEXT content
    }
    categories {
        VARCHAR category_id PK
        VARCHAR category_nm
    }

    %% Reaction Service
    comments {
        BIGINT comment_id PK
        BIGINT book_id
        BIGINT writer_id
        TEXT content
    }
    book_votes {
        BIGINT vote_id PK
        BIGINT book_id
        BIGINT voter_id
    }
    sentence_votes {
        BIGINT vote_id PK
        BIGINT sentence_id
        BIGINT voter_id
    }

    %% Physical Relationships (Within Domain)
    books ||--|{ sentences : "contains"
    categories ||--o{ books : "categorizes"
    comments ||--o{ comments : "replies"
    
    %% Logical Links (Cross-Service)
    users ||..o{ books : "logically creates"
    users ||..o{ sentences : "logically writes"
    users ||..o{ comments : "logically writes"
    users ||..o{ book_votes : "logically votes"
    users ||..o{ sentence_votes : "logically votes"
    
    books ||..o{ comments : "logically has"
    books ||..o{ book_votes : "logically has"
    sentences ||..o{ sentence_votes : "logically has"
```

<br>

---

## PART 1-7. Database Schema

### 🔄 MSA Database Structure

**Database per Service** 패턴을 적용하여, 각 마이크로서비스는 독립적인 데이터베이스를 소유합니다.

| 서비스 | 데이터베이스명 | 주요 테이블 |
|:---:|:---|:---|
| **Member Service** | `next_page_member` | `users`, `refresh_token` |
| **Story Service** | `next_page_story` | `books`, `sentences`, `categories` |
| **Reaction Service** | `next_page_reaction` | `comments`, `book_votes`, `sentence_votes` |

---

## PART 1-8. 팀 그라운드 룰 (XP)

우리 팀은 **Extreme Programming (XP)** 의 가치를 지향하며, 효율적이고 건강한 협업 문화를 만들기 위해 노력합니다.

[👉 팀 그라운드 룰 상세 보기 (GROUND_RULES.md)](GROUND_RULES.md)

- **의사소통:** 상시 공유와 구체적인 질문
- **단순성:** 가독성 좋은 코드와 명확한 설계
- **피드백:** 상호 존중 기반의 코드 리뷰
- **용기:** 문제 공유 및 적극적인 개선 제안
- **존중:** 사람 중심이 아닌 기술 중심의 논의

<br>

---

# PART 2: AS-IS (Monolithic Architecture)

초기 버전의 단일 애플리케이션 아키텍처 (2025.12.23 ~ 2026.01.10)

---

## PART 2-1. Monolithic 아키텍처

### 시스템 구성도

```mermaid
graph TD
    Client[Client Browser]
    App["Spring Boot Application<br/>(8080)"]
    DB[(MariaDB: next_page)]

    Client --> App
    App --> DB
```

### 특징

✅ **장점:** 단순한 배포, 트랜잭션 관리 용이, 빠른 프로토타이핑
❌ **한계:** 낮은 확장성, 장애 전파, 기술 종속성

---

## PART 2-2. Monolithic 실행 방법

⚠️ **상세한 설치 및 실행 가이드, 패키지 구조는 [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)를 참고하세요.**

---

## PART 2-3. Monolithic 패키지 구조

**(생략 - 가이드 문서 참고)**

---

# PART 3: TO-BE (MSA Architecture) ✅ 현재 운영

Microservices Architecture 전환 (2026.01.11 ~)

---

## PART 3-1. MSA 아키텍처

### 전환 배경

1. **확장성:** 특정 기능만 스케일링 불가 (전체 재배포 필요)
2. **장애 격리:** 부분 장애 시 전체 시스템 다운
3. **배포:** 작은 변경도 전체 재배포 필요
4. **팀 협업:** 도메인별 독립 개발 어려움

### 전환 일정

| 날짜 | 작업 | 상태 |
|:---:|:---|:---:|
| 2026-01-11 | Discovery Server, Gateway 구축 | ✅ |
| 2026-01-12 | DB 3개 분리 (member, story, reaction) | ✅ |
| 2026-01-13 | member-service 이관 | ✅ |
| 2026-01-14 | story-service, reaction-service 이관 | ✅ |
| 2026-01-15 | Config Server, Actuator, Feign 적용 | ✅ |

---

## PART 3-2. MSA 시스템 구성도

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

### 서비스별 책임

| 서비스 | 포트 | 주요 책임 |
|:---:|:---:|:---|
| **Config Server** | 8888 | 중앙 설정 관리 (Git Repository 연동) |
| **Discovery Server** | 8761 | Eureka: 서비스 등록/탐색 |
| **Gateway Server** | 8000 | JWT 검증, 라우팅, 헤더 주입 (X-User-Id) |
| **Member Service** | 8081 | 회원가입/로그인, JWT 발급, 회원 정보 관리 |
| **Story Service** | 8082 | 소설 생성/조회, 문장 이어쓰기, WebSocket |
| **Reaction Service** | 8083 | 댓글/대댓글, 개추/비추 투표 |

---

## PART 3-3. MSA 전환 핵심 포인트

1. **Config Server 도입**: 모든 설정(`application.yml`)을 Git에서 중앙 관리.
2. **JPA 객체 참조 → ID 참조 전환**: 서비스 간 결합도 제거.
3. **Application Level Join**: 복잡한 연관관계를 Feign Client로 해결.
4. **Gateway JWT 필터**: 인증/인가 로직을 Gateway로 이관하여 마이크로서비스는 비즈니스 로직에 집중.

---

## PART 3-4. MSA 시퀀스 다이어그램

시스템의 핵심 흐름(Key Flows)을 보여주는 시퀀스 다이어그램입니다.

### 1. 인증 프로세스 & JWT 필터 (Authentication Flow)

사용자가 로그인하여 토큰을 발급받는 과정입니다.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Gateway as API Gateway
    participant Auth as Member Service
    participant DB as Member DB

    User->>Gateway: POST /api/auth/login <br/> {email, password}
    Gateway->>Auth: Route Request
    
    activate Auth
    Auth->>DB: Find User by Email
    DB-->>Auth: Return User Entity
    
    Auth->>Auth: Verify Password (BCrypt)
    Auth->>Auth: Generate Access Code & Refresh Token
    
    Auth-->>Gateway: Return TokenResponse
    deactivate Auth
    
    Gateway-->>User: 200 OK <br/> (Body: AccessToken, Cookie: RefreshToken)
```

### 2. 핵심 로직: 문장 이어쓰기 (Core Feature with Filter)

**Gateway Filter**가 헤더를 변환하고, **Story Service**가 동시성을 제어하며, **WebSocket**이 실시간 전파하는 전체 흐름입니다.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Gateway as API Gateway
    participant Filter as JWT Filter
    participant Story as Story Service
    participant DB as Story DB
    participant Socket as WebSocket Broker

    Note over User, Gateway: Header: Authorization Bearer {Token}

    User->>Gateway: POST /api/books/{id}/sentences
    
    rect rgb(240, 240, 240)
        Note right of Gateway: Gateway Processing
        Gateway->>Filter: Intercept Request
        Filter->>Filter: 1. Validate Token Signature
        Filter->>Filter: 2. Parse Claims (UserId, Email)
        Filter->>Gateway: 3. Add Headers (X-User-Id, X-User-Email)
    end

    Gateway->>Story: Route Request (with X-Headers)
    
    activate Story
    Story->>DB: SELECT * FROM books WHERE id=? <br/> (Pessimistic Lock / @Version)
    DB-->>Story: Return Book + Lock
    
    Story->>Story: Validate Sequence (Current vs Request)
    Story->>Story: Check Previous Writer (Duplicate Prevention)

    Story->>DB: INSERT into sentences
    Story->>DB: UPDATE books SET current_seq++
    
    par Async Notification
        Story->>Socket: Send Message (/topic/books/{id})
        Socket-->>User: Push Notification (New Sentence)
    and Response
        Story-->>Gateway: 201 Created
        Gateway-->>User: 201 Created
    end
    deactivate Story
```

### 3. MSA 통신: 데이터 통합 조회 (Cross-Service Aggregation)

소설 조회 시 **Feign Client**를 통해 타 서비스(Member)의 데이터를 조회하고 병합하는 과정입니다.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Gateway
    participant Story as Story Service
    participant Member as Member Service (Feign)
    
    User->>Gateway: GET /api/books/{id} (Viewer)
    Gateway->>Story: Route Request
    
    activate Story
    Story->>Story: Fetch Book Data (WriterId=100)
    
    rect rgb(240, 248, 255)
        Note right of Story: Cross-Service Communication
        Story->>Member: GET /internal/members/100
        activate Member
        Member-->>Story: Return {nickname: "Hong", ...}
        deactivate Member
        
        Note right of Story: Fallback if Member fails (Circuit Breaker)
    end
    
    Story->>Story: Aggregate Data (Book + Writer Nickname)
    Story-->>Gateway: Return BookDetailDto
    deactivate Story
    
    Gateway-->>User: 200 OK (JSON)
```

---

## PART 3-5. 실행 방법

⚠️ **데이터베이스 설정, 실행 순서, 환경 설정 등 상세 가이드는 [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)를 확인하세요.**

---

## 📚 API 명세 (API Specification)

상세한 REST API 명세(Request/Response 규격)는 별도 문서로 관리됩니다.
[👉 API 상세 명세서 보러가기 (next-page-msa/API_SPECIFICATION.md)](next-page-msa/API_SPECIFICATION.md)

---

Copyright © 2026 **Team Next Page**. All rights reserved.
