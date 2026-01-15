# 📘 Next Page Architecture Master Guide (MSA)
> **문서 등급:** Level 3 (Deep Dive)
> **대상:** 주니어 백엔드 개발자, 시스템 아키텍트를 지망하는 개발자
> **목표:** 이 문서를 정독하면 Next Page 프로젝트의 모든 기술적 의사결정을 이해하고, 타인에게 논리적으로 설명할 수 있다.

---

## 🏗️ 1. 아키텍처 개론: 왜 MSA인가?

### 1-1. Monolithic의 한계와 고통
우리가 처음 만들었던 **Monolithic(단일)** 구조는 `NextPageApplication.java` 하나만 실행하면 모든 기능인 회원, 소설, 댓글 기능이 돌아갔습니다.

*   **문제점 1 (빌드 속도):** "댓글 기능에 오타가 있어서 고쳤는데, 회원가입 기능까지 다시 빌드하고 배포해야 해요."
*   **문제점 2 (장애 전파):** "누가 실시간 채팅(WebSocket)을 미친듯이 써서 메모리가 터졌는데, 멀쩡하던 소설 조회 기능까지 같이 죽었어요."
*   **문제점 3 (기술 종속):** "댓글 서비스는 Node.js로 짜면 더 빠를 것 같은데... 자바로 이미 다 짜여있어서 못 바꿔요."

### 1-2. MSA (Microservices Architecture)로의 진화
우리는 거대한 하나의 덩어리를 **도메인(업무 영역)** 기준으로 쪼갰습니다. 이것이 **Next Page MSA**입니다.

> **💡 핵심 개념:** "도메인(Domain)"이란?
> 소프트웨어로 해결하고자 하는 문제의 영역입니다. 우리는 `Member`(회원), `Story`(소설), `Reaction`(반응) 3개의 도메인으로 나누었습니다.

| 서비스명 | 역할 (Role) | 비유 (Analogy) |
|:---|:---|:---|
| **Member Service** | 회원가입, 로그인, 내 정보 관리 | **동사무소/보안팀:** 신분증(JWT) 발급 및 관리 |
| **Story Service** | 소설 생성, 문장 이어쓰기, 목록 조회 | **도서관/집필실:** 책을 보관하고 작가가 글을 쓰는 곳 |
| **Reaction Service** | 댓글, 대댓글, 좋아요 투표 | **광장/검표소:** 사람들이 떠들고 투표하는 곳 |

---

## 🛠️ 2. 인프라스트럭처 (Infrastructure): 시스템의 뼈대

MSA는 서비스들이 파편화되어 있기 때문에, 이들을 관리해줄 **매니저**들이 필요합니다.

### 2-1. Config Server (설정 관리자)
*   **문제:** 서비스가 3개인데, `application.yml` 설정 파일도 3개입니다. DB 비밀번호를 바꾸려면 3번 수정하고 3번 재배포해야 합니다.
*   **해결:** `NextPage Config Server`를 만들었습니다.
    *   모든 설정(`*.yml`)을 **GitHub**(`next-page-env` 리포지토리) 한곳에 몰아넣습니다.
    *   각 서비스는 켜질 때 Config Server에게 **"내 설정 파일 좀 줘!"** 하고 받아옵니다.
    *   **효과:** 설정이 바뀌면 GitHub 코드만 고치고 빈(Bean)만 리로딩하면 됩니다. (재배포 불필요)

### 2-2. Eureka Discovery Server (위치 추적기)
*   **문제:** MSA에서는 서비스가 클라우드 환경에서 IP가 수시로 바뀔 수 있습니다. `Member Service`가 `Story Service`를 호출하려면 IP를 알아야 하는데, 하드코딩할 수 없습니다.
*   **해결:** **Eureka(유레카)** 라는 전화번호부를 도입했습니다.
    1.  **서비스 등록:** `Story Service`가 켜질 때, "저 여기 있어요! (IP: 192.168.0.5)"라고 Eureka에 신고합니다.
    2.  **서비스 발견:** `Gateway`가 요청을 보낼 때 Eureka에게 "Story Service 어디 있어?"라고 물어보고 찾아갑니다.
    3.  **Client-Side Discovery:** 이 방식은 클라이언트(요청자)가 서비스 위치를 찾는 방식입니다.

### 2-3. API Gateway (정문 & 경비실)
*   **질문:** 클라이언트(프론트엔드)는 `Member`, `Story`, `Reaction` 서비스의 IP를 다 알아야 할까요?
*   **답변:** 아니요! 보안상 위험하고 너무 복잡합니다.
*   **해결:** **Spring Cloud Gateway**를 유일한 출입구로 둡니다. (Port 8000)
    *   **라우팅(Routing):** `/api/books`로 들어오면 `Story Service`로, `/api/auth`로 들어오면 `Member Service`로 토스합니다.
    *   **필터링(Filtering):** 들어오는 요청을 가로채서 **JWT 인증**을 수행합니다. "신분증 안 가져왔어? 돌아가!"

### 2-4. Service Load Balancing (트래픽 분산)
*   **문제:** `Story Service`에 사용자가 너무 몰려서 서버를 하나 더 띄웠습니다(Scale-out). 그럼 게이트웨이나 다른 서비스는 이 두 개의 서버 중 어디로 요청을 보내야 할까요?
*   **해결:** **Client-Side Load Balancing** 방식을 사용합니다. (Spring Cloud LoadBalancer)
    1.  **Eureka 연동:** 게이트웨이와 모든 서비스는 Eureka로부터 "현재 살아있는 서버 명단"을 주기적으로 받아옵니다.
    2.  **자동 분산:** 요청을 보낼 때마다 명단에 있는 서버들에게 번갈아가며(Round Robin 등) 요청을 보냅니다.
    3.  **장점:** 값비싼 하드웨어 로드밸런서(L4/L7 스위치) 없이도 소프트웨어만으로 똑똑하게 트래픽을 분산할 수 있습니다.

---

## 💎 3. 백엔드 핵심 기술 (Core Tech Stack)

### 3-1. Persistence Layer: JPA와 MyBatis의 동거 (CQRS Lite)
우리는 **"쓰기는 정교하게, 읽기는 빠르게"** 라는 철학을 가집니다.

#### 🍊 JPA (Hibernate) - Command (쓰기)
*   **사용처:** `INSERT`, `UPDATE`, `DELETE` 등 데이터의 상태를 변경할 때.
*   **왜 썼나요?**
    *   **객체지향적:** SQL을 몰라도 자바 객체(`Entity`)를 다루듯 DB를 조작할 수 있습니다.
    *   **안전장치:** `Dirty Checking`(변경 감지) 기능 덕분에, 객체 값을 바꾸기만 하면 트랜잭션이 끝날 때 알아서 `UPDATE` 쿼리가 나갑니다.
*   **코드 예시:**
    ```java
    // 자바 코드로만 작성했지만, 실제로는 DB에 INSERT가 발생함
    Book book = Book.create(title, content);
    bookRepository.save(book); 
    ```

#### 🦋 MyBatis - Query (읽기)
*   **사용처:** 복잡한 검색, 통계, 다중 필터링 조회.
*   **왜 썼나요?**
    *   **JPA의 한계:** JPA로 "카테고리가 스릴러이면서, 조회수가 100 이상이고, 최근 3일 내 생성된 글"을 조회하려면 코드가 매우 복잡해지거나 비효율적인 쿼리가 나갑니다.
    *   **SQL 제어권:** MyBatis는 개발자가 SQL `SELECT` 문을 직접 작성합니다. 인덱스를 타게 하거나 튜닝하기에 훨씬 유리합니다.
    *   **동적 쿼리:** `<if test="category != null">` 같은 태그를 써서 상황에 따라 SQL을 자유자재로 바꿀 수 있습니다.

---

## 🔐 4. 보안 아키텍처 (Security Flow)

보안은 **"누구냐(Authentication)"** 와 **"무엇을 할 수 있냐(Authorization)"** 의 싸움입니다.

### 4-1. JWT (JSON Web Token) 전략
우리는 세션(Session)을 쓰지 않습니다. 세션은 서버 메모리에 저장되는데, 서버가 여러 대(MSA)면 세션 동기화가 어렵기 때문입니다. 대신 **Stateless(무상태)** 한 JWT를 씁니다.

1.  **발급:** 로그인 성공 시 `AccessToken`(30분), `RefreshToken`(7일)을 발급합니다.
2.  **저장:** `AccessToken`은 프론트엔드 변수나 헤더에, `RefreshToken`은 보안 쿠키(HttpOnly)에 저장하여 XSS 공격을 방지합니다.

### 4-2. Gateway Security Filter
가장 중요한 부분입니다. 개별 서비스(`Member`, `Story`)는 JWT 검증 로직이 없습니다! 모든 검증은 **Gateway**에서 끝납니다.

*   **1단계:** Gateway Filter가 요청 헤더(`Authorization`)를 낚아챕니다.
*   **2단계:** JWT 서명이 올바른지 확인합니다. (위조된 토큰이면 즉시 `401 Error`)
*   **3단계:** 토큰 안에 있는 유저 ID(`7`)와 역할(`USER`)을 꺼냅니다.
*   **4단계:** 이 정보를 HTTP 헤더 `X-User-Id: 7`, `X-User-Role: USER`로 변환해서 내부 서비스로 넘겨줍니다.
*   **5단계:** 내부 서비스는 복잡한 인증 로직 없이 `request.getHeader("X-User-Id")`만 하면 끝입니다.

---

## 📡 5. 서비스 간 통신 (Inter-Service Communication)

"소설 서비스"가 소설 목록을 보여주려는데, "작성자 닉네임"이 필요합니다. 닉네임은 "회원 서비스"에 있습니다. 어떻게 가져올까요?

### 5-1. OpenFeign: 우아한 REST Client
옛날에는 `RestTemplate`이라는 걸 써서 URL을 직접 문자열로 적었습니다.
```java
// 구방식 (RestTemplate) - 실수하기 딱 좋음
String url = "http://member-service/members/" + writerId;
restTemplate.getForObject(url, MemberDto.class);
```

우리는 **Feign Client**를 씁니다. 그냥 인터페이스만 만들면 됩니다.
```java
// 신방식 (Feign) - 마치 내 옆에 있는 메소드 부르듯이!
@FeignClient(name = "member-service")
public interface MemberClient {
    @GetMapping("/members/{memberId}")
    ApiResponse<MemberDto> getMember(@PathVariable("memberId") Long memberId);
}
```
**작동 원리:** Spring이 런타임에 이 인터페이스의 구현체(Proxy)를 자동으로만들어서, 실제 HTTP 요청을 날려줍니다.

### 5-2. Application Level Join (Aggregation)
MSA의 가장 큰 단점은 **JOIN을 못한다**는 것입니다. DB가 쪼개져 있으니까요. 그래서 우리는 **"메모리에서 조립"** 합니다.

1.  **Story Service:** DB에서 소설 목록 10개를 가져옵니다. (작성자 ID만 있음: `[1, 5, 3 ...]`)
2.  **Feign Call:** "회원 서비스님, ID가 1, 3, 5인 회원 닉네임 다 주세요." (Batch 요청)
3.  **Data Map:** 받아온 회원 정보를 Map으로 만듭니다. `{1: "철수", 3: "영희", 5: "바둑이"}`
4.  **Assemble:** 소설 목록을 반복문 돌면서 Map에서 닉네임을 꺼내 채워 넣습니다.
5.  **Return:** 완성된 데이터를 프론트엔드에 줍니다.

---

## ⚡ 6. 실시간 통신 (Real-Time Architecture)

### 6-1. Ajax vs WebSocket: 초보를 위한 비유
*   **Ajax (HTTP):** **"무전기"** 같습니다. 버튼을 누르고 말을 해야(Request) 상대방이 대답(Response)합니다. 내가 가만히 있으면 아무 소식도 못 듣습니다. "새 글 올라왔나요?"라고 계속 물어봐야 합니다(Polling).
*   **WebSocket:** **"전화기"** 같습니다. 한번 연결되면(Handshake), 상대방이 아무 때나 말을 걸 수 있습니다. 서버가 "야! 새 글 올라왔어!"라고 먼저 알려줄 수 있습니다(Push).

### 6-2. STOMP 프로토콜
WebSocket은 그냥 "선"만 연결해주는 것입니다. 그 안에서 어떤 규칙으로 대화할지 정해야 하는데, 우리는 **STOMP** 규약을 씁니다. 이것은 **"우체통 규칙"** 과 비슷합니다.

*   **PUB (발행):** "이 편지를 `1번 우체통(/topic/books/1)`에 넣어주세요."
*   **SUB (구독):** "저는 `1번 우체통`에 편지가 오면 받을래요."

### 6-3. 구현 시나리오 (타이핑 인디케이터)
1.  **유저 A**가 키보드를 칩니다.
2.  프론트엔드 JS가 소켓으로 보냅니다. `SEND /app/typing`, 내용: `{ "user": "A", "status": "WRITING" }`
3.  백엔드(Story Service)가 받아서 그대로 뿌립니다. `BROADCAST /topic/books/1`
4.  같은 소설을 보고 있던 **유저 B**의 JS가 메시지를 받아서 화면에 "A님이 작성 중..."을 띄웁니다.
5.  **핵심:** 이 정보는 DB에 저장하지 않습니다. 휘발성 데이터이기 때문입니다.

---

## 📂 7. 공통 모듈 (Common Module) 전략

### 7-1. 왜 만들었나요?
MSA를 하다 보면 `MemberService`에서도 `ErrorResponse` 클래스가 필요하고, `StoryService`에서도 필요합니다. 복사-붙여넣기를 하면 나중에 고칠 때 지옥이 펼쳐집니다.

### 7-2. 무엇이 들어있나요?
*   **GlobalException:** 모든 서비스가 에러를 낼 때 똑같은 JSON 모양(`code`, `message`)으로 내보내게 하는 처리기.
*   **Standard DTOs:** 서비스끼리 데이터를 주고받을 때 쓰는 택배 상자 규격 (`MemberInfoDto` 등).
*   **Utils:** 날짜 변환기, 보안 헤더 파서 등 잡동사니 도구들.

이 모듈은 `.jar` 파일로 빌드되어 다른 서비스들이 `build.gradle`에서 가져다 씁니다.

---

## 📝 마치며: 이 문서를 읽은 당신이 할 수 있는 말
이제 누군가 **"Next Page 프로젝트는 어떻게 구성되어 있나요?"** 라고 물으면 이렇게 대답하세요.

> "저희는 **확장성**과 **장애 격리**를 위해 **Spring Cloud 기반의 MSA**를 도입했습니다.
> **Gateway**를 통해 보안을 단일화했고, 데이터 조작은 **JPA**, 복잡한 조회는 **MyBatis**를 사용하는 **Hybrid 패턴**을 적용했습니다.
> 서비스 간 통신은 **Feign Client**를 통해 **Application Level Join**을 수행하며, 사용자 경험을 위해 **WebSocket(STOMP)** 으로 실시간 타이핑과 업데이트 기능을 구현했습니다."

---
