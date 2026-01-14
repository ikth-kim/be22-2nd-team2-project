# 🎯 MSA 전환 완료 상태 문서

> **Last Updated:** 2026-01-16
> **Status:** ✅ 빌드 완료, 안정성(Circuit Breaker) 확보, 프로덕션 준비 완료

---

## 📊 전환 완료 현황

### ✅ 완료된 작업

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
| **빌드** | 전체 MSA 빌드 | ✅ 성공 | 43 tasks, 21초 |

---

## 🏗️ MSA 아키텍처 구조

```
                    [Eureka Discovery Server]
                           Port: 8761
                                |
                    [API Gateway Server]
                         Port: 8000
                      (JWT 검증 & 라우팅)
                                |
            ┌───────────────────┼───────────────────┐
            │                   │                   │
    [Member Service]    [Story Service]    [Reaction Service]
       Port: 8081          Port: 8082          Port: 8083
            │                   │                   │
    [DB: member]         [DB: story]        [DB: reaction]
            │                   │                   │
            └───────────────────┴───────────────────┘
                    Feign Client 통신 (동기)
               + Resilience4j (Circuit Breaker)
```

### 서비스 간 의존성

```
member-service (독립)
     ↑
     │ (Feign + CB)
     │
story-service ────┐
     ↑            │
     │ (Feign+CB) │ (Feign+CB)
     │            │
reaction-service ─┘
```

---

## 🔄 Monolithic → MSA 전환 핵심 변경사항

### 1. **JPA 관계 제거 → ID 참조 전환**

#### Before (Monolithic)

```java
// ❌ 객체 참조 (Cross-DB JOIN 불가)
@Entity
public class Book {
    @ManyToOne
    @JoinColumn(name = "writer_id")
    private Member writer;  // JPA 객체 참조

    public String getWriterNickname() {
        return writer.getUserNicknm();
    }
}
```

#### After (MSA)

```java
// ✅ ID 참조 + Feign Client
@Entity
public class Book {
    @Column(name = "writer_id")
    private Long writerId;  // ID만 저장

    // DTO에서 Feign Client로 닉네임 조회
    // (Service Layer에서 처리)
}
```

### 2. **MyBatis JOIN 제거 → Application Level Join**

#### Before (Monolithic)

```xml
<!-- ❌ Cross-DB JOIN (MSA에서 불가능) -->
<select id="findBookForViewer" resultType="BookDetailDto">
    SELECT
        b.book_id,
        b.title,
        u.user_nicknm AS writerNicknm  <!-- users 테이블 JOIN -->
    FROM books b
    LEFT JOIN users u ON b.writer_id = u.user_id
    WHERE b.book_id = #{bookId}
</select>
```

#### After (MSA)

```xml
<!-- ✅ 자신의 DB만 조회 -->
<select id="findBookForViewer" resultType="BookDetailDto">
    SELECT
        b.book_id,
        b.title,
        b.writer_id  <!-- ID만 조회 -->
    FROM books b
    WHERE b.book_id = #{bookId}
</select>
```

```java
// Service Layer에서 Feign Client로 회원 정보 조회
@Service
public class BookQueryService {
    private final BookMapper bookMapper;
    private final MemberServiceClient memberServiceClient;

    public BookDetailDto getBookForViewer(Long bookId) {
        // 1. 자신의 DB에서 소설 정보 조회
        BookDetailDto book = bookMapper.findBookForViewer(bookId, userId);

        // 2. Feign Client로 member-service에서 작성자 정보 조회
        // Circuit Breaker가 적용되어 장애 시 Fallback 처리됨
        ApiResponse<MemberInfoDto> response =
            memberServiceClient.getMemberInfo(book.getWriterId());

        // 3. DTO 병합
        book.setWriterNicknm(response.getData().getUserNicknm());

        return book;
    }
}
```

### 3. **N+1 문제 방지: Batch API 구현**

```java
// ✅ 일괄 조회로 성능 최적화
public BookDetailDto getBookForViewer(Long bookId) {
    BookDetailDto book = bookMapper.findBookForViewer(bookId, userId);
    List<SentenceDto> sentences = bookMapper.findSentencesByBookId(bookId, userId);

    // 모든 작성자 ID 수집 (중복 제거)
    List<Long> writerIds = sentences.stream()
        .map(SentenceDto::getWriterId)
        .distinct()
        .collect(Collectors.toList());

    if (book.getWriterId() != null) {
        writerIds.add(book.getWriterId());
    }

    // 한 번의 Feign 호출로 모든 회원 정보 조회
    ApiResponse<MemberBatchInfoDto> response =
        memberServiceClient.getMembersBatch(writerIds);

    Map<Long, String> memberMap = response.getData().getMembers().stream()
        .collect(Collectors.toMap(
            MemberInfoDto::getUserId,
            MemberInfoDto::getUserNicknm
        ));

    // 닉네임 매핑
    book.setWriterNicknm(memberMap.get(book.getWriterId()));
    sentences.forEach(s -> s.setWriterNicknm(memberMap.get(s.getWriterId())));

    book.setSentences(sentences);
    return book;
}
```

### 4. **SecurityUtil 헤더 기반 변경**

#### Before (Monolithic)

```java
// ❌ Spring Security Context 사용
public static Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
    return user.getUserId();
}
```

#### After (MSA)

```java
// ✅ Gateway가 주입한 HTTP 헤더 사용
public static Long getCurrentUserId() {
    HttpServletRequest request = getCurrentRequest();
    String userIdHeader = request.getHeader("X-User-Id");
    if (userIdHeader == null) {
        throw new BusinessException(ErrorCode.UNAUTHENTICATED);
    }
    return Long.parseLong(userIdHeader);
}
```

### 5. **Resilience (Circuit Breaker) 적용**

```yaml
resilience4j:
  circuitbreaker:
    instances:
      memberService:
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

```java
// Fallback Method 예시
public MemberInfoDto getMemberInfoFallback(Long userId, Exception ex) {
    log.warn("Member service unavailable: {}", ex.getMessage());
    return new MemberInfoDto(userId, "Unknown User", "USER");
}
```

---

**Status:** ✅ Production Ready
**Next Steps:** 모니터링 시스템(Prometheus/Grafana) 연동 고려
