# 🤖 Next Page Project AI Guidelines

이 문서는 AI 어시스턴트가 'Next Page' 프로젝트의 코드를 작성하거나 수정할 때 준수해야 할 핵심 원칙과 규칙을 정의합니다.

## 1. 🎯 프로젝트 정체성 및 아키텍처
*   **프로젝트명:** Next Page (릴레이 소설 창작 플랫폼)
*   **핵심 철학:** 도메인 주도 설계(DDD) + 명령/조회 책임 분리(CQRS)
*   **기술 스택:** Java 17, Spring Boot 3.5.9, MariaDB
*   **ORM 전략:**
    *   **Command (CUD):** JPA (Hibernate) 사용. 객체 지향적 로직 구현, Dirty Checking 활용.
    *   **Query (R):** MyBatis 사용. 복잡한 조회, 조인, DTO 직접 매핑 성능 최적화.

## 2. 🏛️ 코딩 컨벤션 및 패턴

### 2.1 Entity & Domain Logic (DDD)
*   **Setter 사용 금지:** Entity에는 `@Setter`를 절대 사용하지 않는다. 상태 변경은 명확한 의도를 가진 메서드(Business Method)로 구현한다.
    *   *Bad:* `book.setStatus("COMPLETED");`
    *   *Good:* `book.completeStory();`
*   **생성자:** `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 기본으로 사용하며, 필요한 필드만 받는 `@Builder`를 별도로 구현한다.
*   **도메인 로직 위치:** 비즈니스 규칙은 Service가 아닌 **Entity 내부**에 위치시킨다.
    *   Ex) '다음 순서인지 확인', '소설 완결 조건 체크' 등은 Entity 메서드로 구현.

### 2.2 Layered Architecture Rules
*   **Controller:** 요청값 검증(`@Valid`), 응답 변환(Representation)만 담당. 로직 포함 금지.
*   **Service:** 트랜잭션 관리(`@Transactional`) 및 도메인 객체 간의 협력 조율(Orchestration).
    *   순수 비즈니스 로직은 Entity에 위임하고, Service는 그것을 호출하는 형태.
*   **DTO:**
    *   Entity를 직접 반환하지 않는다 (**Strict Rule**).
    *   Request/Response DTO는 `record` 또는 `static class`로 정의하여 불변성을 유지 권장.
    *   JPA Entity <-> DTO 변환은 `ModelMapper`나 생성자/Builder 패턴 사용.

### 2.3 CQRS 구현 규칙
*   **Command (쓰기):**
    *   Repository: `JpaRepository` 상속.
    *   복잡한 연관관계 매핑 및 생명주기가 같은 애그리거트는 JPA Cascade 활용.
*   **Query (읽기):**
    *   Mapper: `MyBatis` Mapper Interface 사용 (`@Mapper`).
    *   XML 위치: `resources/mapper/**/*.xml`.
    *   쿼리 결과는 Entity가 아닌 **조회 전용 DTO**로 즉시 매핑.

### 2.4 공통 필드 및 응답
*   **BaseEntity:** `created_at`, `updated_at` 등 공통 감사(Audit) 필드는 `@MappedSuperclass`로 관리.
*   **API Response:** 성공/실패 여부를 포함한 공통 래퍼(Wrapper) 클래스 사용 (프로젝트 내 정의된 포맷 준수).

### 2.5 Security & JWT
*   **Dependency:** `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.11.5 버전 권장).
*   **Authentication:** `Bearer` Token 방식 사용. Header의 `Authorization` 필드 파싱.

## 3. 🛠️ 라이브러리 및 도구 활용
*   **Lombok:** `@Getter`, `@RequiredArgsConstructor`, `@Slf4j` 적극 활용. `@ToString`은 순환 참조 주의(exclude 설정).
*   **Validation:** `jakarta.validation` 어노테이션(`@NotNull`, `@Size`, `@Email`)으로 입력값 검증 수행.
*   **Soft Delete:** 회원은 DB에서 즉시 삭제하지 않고 `user_status`를 `DELETED`로 업데이트. 쿼리 시 이를 항상 고려해야 함 (`@Where` 또는 쿼리 조건 추가).

## 4. 📝 주석 및 커밋 가이드
*   **JavaDoc:** 클래스 및 주요 public 메서드에는 기능, 파라미터, 반환값에 대한 설명을 기술.
*   **Commit Message:** `type: subject` 형식을 준수 (README.md의 Convention 섹션 참조).
    *   `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`

---
**⚠️ 주의:** 위 규칙을 위반하는 코드를 제안하지 마십시오. 특히 JPA와 MyBatis의 혼용 전략(CQRS)을 어기거나, Entity에 Setter를 남발하는 행위는 엄격히 금지됩니다.
