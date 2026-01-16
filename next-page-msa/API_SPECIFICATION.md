# 📋 Next Page API Specification
> **버전:** 3.0 (MSA Complete + Internal API)
> **최신 업데이트:** 2026-01-16
> **설명:** Next Page MSA 프로젝트의 전체 REST API, WebSocket, Internal API 명세서입니다.

## 📖 목차
1. [회원 (Member) 도메인](#-1-회원-member-도메인)
2. [소설 (Story) 도메인](#-2-소설-story-도메인)
3. [반응 (Reaction) 도메인](#-3-반응-reaction-도메인)
4. [실시간 (WebSocket/STOMP) 도메인](#-4-실시간-websocketstomp-도메인)
5. [내부 API (Internal API - Feign Client)](#-5-내부-api-internal-api---feign-client)
6. [보안 및 인증](#-6-보안-및-인증)
7. [에러 응답 형식](#-7-에러-응답-형식)

---

## 🏗️ 1. 회원 (Member) 도메인

### 회원가입
- **API ID**: PGM-AUTH-SIGNUP
- **1 DEPTH**: 회원
- **2 DEPTH**: 인증
- **3 DEPTH**: 회원가입
- **설명**: 신규 일반 회원을 등록한다.
- **method**: `POST`
- **URL**: `/api/auth/signup`
- **권한**: `비회원`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userEmail | STRING | O | 사용자 이메일 (로그인 ID) |
| userPw | STRING | O | 비밀번호 |
| userNicknm | STRING | O | 사용자 닉네임 |

☑ **Response**: `String` ("회원가입 성공")

---

### 관리자 회원가입 (신청)
- **API ID**: PGM-AUTH-ADMIN-SIGNUP
- **1 DEPTH**: 회원
- **2 DEPTH**: 인증
- **3 DEPTH**: 관리자 가입
- **설명**: 신규 관리자 계정을 신청한다. 가입 직후에는 **PENDING (승인 대기)** 상태이며, 기존 관리자의 승인이 있어야 로그인이 가능하다.
- **method**: `POST`
- **URL**: `/api/auth/admin`
- **권한**: `비회원`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userEmail | STRING | O | 관리자 이메일 |
| userPw | STRING | O | 비밀번호 |
| userNicknm | STRING | O | 관리자 닉네임 |

☑ **Response**: `String` ("관리자 가입 신청 성공 (승인 대기)")

---

### 관리자 승인 (기존 관리자 전용)
- **API ID**: PGM-ADMIN-APPROVE
- **1 DEPTH**: 회원
- **2 DEPTH**: 관리
- **3 DEPTH**: 계정 승인
- **설명**: 승인 대기(PENDING) 상태인 관리자 계정을 승인(ACTIVE)하여 활성화한다.
- **method**: `PATCH`
- **URL**: `/api/auth/admin/approve/{userId}`
- **권한**: `관리자`

☑ **Response**: `String` ("관리자 승인 완료")

---

### 로그인
- **API ID**: PGM-AUTH-LOGIN
- **1 DEPTH**: 회원
- **2 DEPTH**: 인증
- **3 DEPTH**: 로그인
- **설명**: 이메일과 비밀번호로 로그인하여 JWT(Access/Refresh Token)를 발급받는다.
- **method**: `POST`
- **URL**: `/api/auth/login`
- **권한**: `비회원`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userEmail | STRING | O | 사용자 이메일 |
| userPw | STRING | O | 비밀번호 |

☑ **Response Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| accessToken | STRING | 액세스 토큰 |
| refreshToken | STRING | 리프레시 토큰 |

---

### 토큰 갱신
- **API ID**: PGM-AUTH-REFRESH
- **1 DEPTH**: 회원
- **2 DEPTH**: 인증
- **3 DEPTH**: 토큰 갱신
- **설명**: 쿠키의 Refresh Token을 이용해 새로운 Access Token을 발급받는다.
- **method**: `POST`
- **URL**: `/api/auth/refresh`
- **권한**: `회원`, `관리자`

☑ **Request**: Cookie (`refreshToken`)
☑ **Response**: 새로운 Access Token 및 Refresh Token

---

### 로그아웃
- **API ID**: PGM-AUTH-LOGOUT
- **1 DEPTH**: 회원
- **2 DEPTH**: 인증
- **3 DEPTH**: 로그아웃
- **설명**: Refresh Token을 무효화하고 쿠키를 삭제한다.
- **method**: `POST`
- **URL**: `/api/auth/logout`
- **권한**: `회원`, `관리자`

---

### 회원 탈퇴 (본인)
- **API ID**: PGM-MEMBER-WITHDRAW
- **1 DEPTH**: 회원
- **2 DEPTH**: 계정 관리
- **3 DEPTH**: 탈퇴
- **설명**: 현재 로그인한 사용자의 계정을 탈퇴(삭제)한다.
- **method**: `DELETE`
- **URL**: `/api/auth/withdraw`
- **권한**: `회원`, `관리자`

---

### 회원 강제 탈퇴 (관리자)
- **API ID**: PGM-ADMIN-KICK
- **1 DEPTH**: 회원
- **2 DEPTH**: 관리
- **3 DEPTH**: 강제 탈퇴
- **설명**: 관리자가 특정 회원을 강제로 탈퇴시킨다.
- **method**: `DELETE`
- **URL**: `/api/auth/admin/users/{userId}`
- **권한**: `관리자`

---

### 이메일 중복 체크
- **API ID**: PGM-CHECK-EMAIL
- **1 DEPTH**: 회원
- **2 DEPTH**: 검증
- **3 DEPTH**: 이메일 체크
- **설명**: 이메일 중복 여부를 확인한다.
- **method**: `GET`
- **URL**: `/api/auth/check-email`
- **권한**: `비회원`

☑ **Request Parameters (Query)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| email | STRING | O | 중복 확인할 이메일 |

---

### 닉네임 중복 체크
- **API ID**: PGM-CHECK-NICK
- **1 DEPTH**: 회원
- **2 DEPTH**: 검증
- **3 DEPTH**: 닉네임 체크
- **설명**: 닉네임 중복 여부를 확인한다.
- **method**: `GET`
- **URL**: `/api/auth/check-nickname`
- **권한**: `비회원`

☑ **Request Parameters (Query)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| nickname | STRING | O | 중복 확인할 닉네임 |

---

### 내 정보 조회 (마이페이지)
- **API ID**: PGM-MEMBER-ME
- **1 DEPTH**: 회원
- **2 DEPTH**: 조회
- **3 DEPTH**: 내 정보
- **설명**: 로그인한 사용자의 상세 정보를 조회한다.
- **method**: `GET`
- **URL**: `/api/members/me`
- **권한**: `회원`, `관리자`

☑ **Response Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| memberId | INTEGER | ID |
| email | STRING | 이메일 |
| nickname | STRING | 닉네임 |

---

### 내 활동 내역 조회
- **API ID**: PGM-MEMBER-ACTIVITY
- **1 DEPTH**: 회원
- **2 DEPTH**: 조회
- **3 DEPTH**: 활동 내역
- **설명**: 내가 쓴 소설, 문장, 댓글 등 활동 내역을 조회한다.
- **method**: `GET`
- **URL**: `/api/members/me/activities`
- **권한**: `회원`, `관리자`

---

## 📕 2. 소설 (Story) 도메인

### 카테고리 목록 조회
- **API ID**: PGM-CAT-LIST
- **1 DEPTH**: 소설
- **2 DEPTH**: 카테고리
- **3 DEPTH**: 목록 조회
- **설명**: 소설 카테고리 전체 목록을 조회한다.
- **method**: `GET`
- **URL**: `/api/categories`
- **권한**: `모두`

---

### 소설 생성
- **API ID**: PGM-BOOK-CREATE
- **1 DEPTH**: 소설
- **2 DEPTH**: 관리
- **3 DEPTH**: 생성
- **설명**: 새로운 릴레이 소설을 생성한다.
- **method**: `POST`
- **URL**: `/api/books`
- **권한**: `회원`, `관리자`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| title | STRING | O | 소설 제목 |
| categoryId | STRING | O | 카테고리 ID |
| maxSequence | INTEGER | O | 목표 문장 수 (예: 50) |
| firstSentence | STRING | O | 첫 문장 |

---

### 소설 목록 조회
- **API ID**: PGM-BOOK-LIST
- **1 DEPTH**: 소설
- **2 DEPTH**: 조회
- **3 DEPTH**: 목록
- **설명**: 조건에 따라 소설 목록을 페이징하여 조회한다.
- **method**: `GET`
- **URL**: `/api/books`
- **권한**: `모두`

☑ **Request Parameters (Query)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| page | INTEGER | X | 페이지 번호 (Default: 0) |
| size | INTEGER | X | 페이지 크기 (Default: 10) |
| keyword | STRING | X | 검색어 |
| categoryId | STRING | X | 카테고리 필터 |

---

### 소설 상세 조회
- **API ID**: PGM-BOOK-DETAIL
- **1 DEPTH**: 소설
- **2 DEPTH**: 조회
- **3 DEPTH**: 상세
- **설명**: 소설의 기본 정보를 조회한다.
- **method**: `GET`
- **URL**: `/api/books/{bookId}`
- **권한**: `모두`

---

### 소설 뷰어 조회
- **API ID**: PGM-BOOK-VIEW
- **1 DEPTH**: 소설
- **2 DEPTH**: 조회
- **3 DEPTH**: 뷰어 (전체 문장)
- **설명**: 소설의 전체 내용(문장 리스트 포함)을 뷰어 모드로 조회한다.
- **method**: `GET`
- **URL**: `/api/books/{bookId}/view`
- **권한**: `모두`

---

### 문장 이어쓰기
- **API ID**: PGM-BOOK-APPEND
- **1 DEPTH**: 소설
- **2 DEPTH**: 문장
- **3 DEPTH**: 생성 (이어쓰기)
- **설명**: 진행 중인 소설에 새로운 문장을 추가한다.
- **method**: `POST`
- **URL**: `/api/books/{bookId}/sentences`
- **권한**: `회원`, `관리자`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| content | STRING | O | 이어쓸 문장 내용 |

---

### 소설 정보 수정
- **API ID**: PGM-BOOK-UPDATE
- **1 DEPTH**: 소설
- **2 DEPTH**: 관리
- **3 DEPTH**: 수정
- **설명**: 소설 제목을 수정한다. (작성자/관리자)
- **method**: `PATCH`
- **URL**: `/api/books/{bookId}`
- **권한**: `회원 (작성자)`, `관리자`

---

### 문장 수정
- **API ID**: PGM-SENTENCE-UPDATE
- **1 DEPTH**: 소설
- **2 DEPTH**: 문장
- **3 DEPTH**: 수정
- **설명**: 특정 문장의 내용을 수정한다. (작성자/관리자)
- **method**: `PATCH`
- **URL**: `/api/books/{bookId}/sentences/{sentenceId}`
- **권한**: `회원 (작성자)`, `관리자`

---

### 문장 삭제
- **API ID**: PGM-SENTENCE-DELETE
- **1 DEPTH**: 소설
- **2 DEPTH**: 문장
- **3 DEPTH**: 삭제
- **설명**: 특정 문장을 삭제한다. 순서가 자동 재정렬된다. (작성자/관리자)
- **method**: `DELETE`
- **URL**: `/api/books/{bookId}/sentences/{sentenceId}`
- **권한**: `회원 (작성자)`, `관리자`

---

### 소설 완결 처리
- **API ID**: PGM-BOOK-COMPLETE
- **1 DEPTH**: 소설
- **2 DEPTH**: 관리
- **3 DEPTH**: 완결
- **설명**: 작성자가 소설을 수동으로 완결 처리한다.
- **method**: `POST`
- **URL**: `/api/books/{bookId}/complete`
- **권한**: `회원 (작성자)`, `관리자`

---

### 내가 쓴 문장 조회
- **API ID**: PGM-MY-SENTENCES
- **1 DEPTH**: 소설
- **2 DEPTH**: 조회
- **3 DEPTH**: 내 문장
- **설명**: 로그인한 사용자가 작성한 문장들을 조회한다.
- **method**: `GET`
- **URL**: `/api/books/mysentences`
- **권한**: `회원`, `관리자`

---

## 💬 3. 반응 (Reaction) 도메인

### 댓글 작성
- **API ID**: PGM-COMMENT-CREATE
- **1 DEPTH**: 반응
- **2 DEPTH**: 댓글
- **3 DEPTH**: 작성
- **설명**: 소설에 댓글을 작성한다 (대댓글 가능).
- **method**: `POST`
- **URL**: `/api/reactions/comments`
- **권한**: `회원`, `관리자`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| bookId | INTEGER | O | 소설 ID |
| content | STRING | O | 내용 |
| parentId | INTEGER | X | 상위 댓글 ID (대댓글일 경우) |

---

### 댓글 수정
- **API ID**: PGM-COMMENT-UPDATE
- **1 DEPTH**: 반응
- **2 DEPTH**: 댓글
- **3 DEPTH**: 수정
- **설명**: 댓글 내용을 수정한다.
- **method**: `PATCH`
- **URL**: `/api/reactions/comments/{commentId}`
- **권한**: `회원 (작성자)`, `관리자`

---

### 댓글 삭제
- **API ID**: PGM-COMMENT-DELETE
- **1 DEPTH**: 반응
- **2 DEPTH**: 댓글
- **3 DEPTH**: 삭제
- **설명**: 댓글을 삭제한다.
- **method**: `DELETE`
- **URL**: `/api/reactions/comments/{commentId}`
- **권한**: `회원 (작성자)`, `관리자`

---

### 댓글 목록 조회
- **API ID**: PGM-COMMENT-LIST
- **1 DEPTH**: 반응
- **2 DEPTH**: 댓글
- **3 DEPTH**: 목록 조회
- **설명**: 특정 소설의 댓글 목록을 조회한다.
- **method**: `GET`
- **URL**: `/api/reactions/comments/{bookId}`
- **권한**: `모두`

---

### 내가 쓴 댓글 조회
- **API ID**: PGM-MY-COMMENTS
- **1 DEPTH**: 반응
- **2 DEPTH**: 댓글
- **3 DEPTH**: 내 댓글 조회
- **설명**: 내가 작성한 댓글 목록을 페이징하여 조회한다.
- **method**: `GET`
- **URL**: `/api/reactions/mycomments`
- **권한**: `회원`, `관리자`

---

### 소설 투표
- **API ID**: PGM-VOTE-BOOK
- **1 DEPTH**: 반응
- **2 DEPTH**: 투표
- **3 DEPTH**: 소설 투표
- **설명**: 소설에 대해 투표를 하거나 취소한다. (좋아요/싫어요)
- **method**: `POST`
- **URL**: `/api/reactions/votes/books`
- **권한**: `회원`, `관리자`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| bookId | INTEGER | O | 소설 ID |
| voteType | STRING | O | 'LIKE' or 'DISLIKE' |

---

### 문장 투표
- **API ID**: PGM-VOTE-SENTENCE
- **1 DEPTH**: 반응
- **2 DEPTH**: 투표
- **3 DEPTH**: 문장 투표
- **설명**: 특정 문장에 대해 투표를 하거나 취소한다.
- **method**: `POST`
- **URL**: `/api/reactions/votes/sentences/{sentenceId}`
- **권한**: `회원`, `관리자`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| voteType | STRING | O | 'LIKE' or 'DISLIKE' |

---

## ⚡ 4. 실시간 (WebSocket/STOMP) 도메인

### WebSocket 연결
- **ID**: WS-CONNECT
- **1 DEPTH**: 실시간
- **2 DEPTH**: 연결
- **3 DEPTH**: Handshake
- **설명**: WebSocket 연결을 수립한다. (Story Service: 8082)
- **URL**: `/ws`

---

### 타이핑 상태 전송 (문장)
- **ID**: WS-TYPING-SENTENCE
- **1 DEPTH**: 실시간
- **2 DEPTH**: 알림
- **3 DEPTH**: 문장 작성 중
- **설명**: 사용자가 문장을 작성 중일 때 상태를 전송한다.
- **Send To**: `/app/typing/sentence`
- **Subscribe**: `/topic/typing/sentence/{bookId}`

---

### 타이핑 상태 전송 (댓글)
- **ID**: WS-TYPING-COMMENT
- **1 DEPTH**: 실시간
- **2 DEPTH**: 알림
- **3 DEPTH**: 댓글 작성 중
- **설명**: 사용자가 댓글을 작성 중일 때 상태를 전송한다.
- **Send To**: `/app/typing/comment`
- **Subscribe**: `/topic/typing/comment/{bookId}`

---

### 새 소설 생성 알림
- **ID**: WS-BOOK-CREATED
- **1 DEPTH**: 실시간
- **2 DEPTH**: 알림
- **3 DEPTH**: 소설 생성
- **설명**: 새로운 소설 생성 시 메인 페이지에 알림을 수신한다.
- **Subscribe**: `/topic/books/new`

---

### 새 문장 추가 알림
- **ID**: WS-SENTENCE-CREATED
- **1 DEPTH**: 실시간
- **2 DEPTH**: 알림
- **3 DEPTH**: 문장 추가
- **설명**: 새 문장이 추가되었을 때 알림을 수신한다.
- **Subscribe**: `/topic/sentences/{bookId}`

---

### 새 댓글 추가 알림
- **ID**: WS-COMMENT-CREATED
- **1 DEPTH**: 실시간
- **2 DEPTH**: 알림
- **3 DEPTH**: 댓글 추가
- **설명**: 새 댓글이 추가되었을 때 알림을 수신한다. (Reaction Service → Story Service WebSocket)
- **Subscribe**: `/topic/comments/{bookId}`

☑ **Message Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| commentId | LONG | 댓글 ID |
| bookId | LONG | 소설 ID |
| content | STRING | 댓글 내용 |
| nickname | STRING | 작성자 닉네임 |
| createdAt | DATETIME | 작성일시 |

---

### 소설 완결 알림
- **ID**: WS-BOOK-COMPLETED
- **1 DEPTH**: 실시간
- **2 DEPTH**: 알림
- **3 DEPTH**: 소설 완결
- **설명**: 소설이 완결되었을 때 알림을 수신한다.
- **Subscribe**: `/topic/books/{bookId}/status`

---

### 투표 업데이트 알림
- **ID**: WS-VOTE-UPDATED
- **1 DEPTH**: 실시간
- **2 DEPTH**: 알림
- **3 DEPTH**: 투표 업데이트
- **설명**: 소설/문장에 투표가 발생했을 때 실시간으로 투표 수를 수신한다.
- **Subscribe**: `/topic/books/{bookId}/votes`

☑ **Message Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| targetId | LONG | 대상 ID (소설 ID 또는 문장 ID) |
| targetType | STRING | 대상 타입 (BOOK/SENTENCE) |
| likeCount | INTEGER | 좋아요 수 |
| dislikeCount | INTEGER | 싫어요 수 |

---

## 📌 5. 내부 API (Internal API - Feign Client)

> **중요:** 이 섹션의 API는 MSA 서비스 간 내부 통신용으로, Gateway를 거치지 않고 직접 호출됩니다.
> 모든 서비스의 SecurityConfig에서 `/internal/**` 경로는 `permitAll()` 처리되어 있습니다.

### 5-1. Member Service Internal API

#### 회원 정보 조회 (단건)
- **API ID**: INTERNAL-MEMBER-GET
- **1 DEPTH**: 내부
- **2 DEPTH**: 회원
- **3 DEPTH**: 단건 조회
- **설명**: 타 서비스에서 특정 회원의 정보를 조회합니다.
- **사용처**: Story Service (소설/문장 작성자 조회), Reaction Service (댓글/투표 작성자 조회)
- **method**: `GET`
- **URL**: `/internal/members/{memberId}`
- **권한**: `System (내부 통신)`

☑ **Response Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| userId | LONG | 회원 ID |
| nickname | STRING | 닉네임 |
| role | STRING | 역할 (USER/ADMIN) |

---

#### 회원 정보 조회 (Batch)
- **API ID**: INTERNAL-MEMBER-BATCH
- **1 DEPTH**: 내부
- **2 DEPTH**: 회원
- **3 DEPTH**: 일괄 조회
- **설명**: N+1 문제 방지를 위해 여러 회원의 정보를 한 번에 조회합니다.
- **사용처**: Story Service (목록 조회 시), Reaction Service (댓글 목록 조회 시)
- **method**: `GET`
- **URL**: `/internal/members/batch`
- **권한**: `System (내부 통신)`

☑ **Request Parameters (Query)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userIds | LIST<LONG> | O | 회원 ID 리스트 (예: ?userIds=1,2,3) |

☑ **Response Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| members | MAP<LONG, MemberInfoDto> | Key: userId, Value: 회원 정보 |

**Response 예시:**
```json
{
  "code": "200",
  "message": "success",
  "data": {
    "members": {
      "1": {"userId": 1, "nickname": "철수", "role": "USER"},
      "2": {"userId": 2, "nickname": "영희", "role": "ADMIN"}
    }
  }
}
```

---

#### 회원 존재 여부 확인
- **API ID**: INTERNAL-MEMBER-EXISTS
- **1 DEPTH**: 내부
- **2 DEPTH**: 회원
- **3 DEPTH**: 존재 확인
- **설명**: 특정 회원이 존재하는지 확인합니다.
- **사용처**: Story Service, Reaction Service (유효성 검증)
- **method**: `GET`
- **URL**: `/internal/members/{memberId}/exists`
- **권한**: `System (내부 통신)`

☑ **Response**: `Boolean` (true: 존재, false: 미존재)

---

### 5-2. Story Service Internal API

#### 소설 정보 조회 (단건)
- **API ID**: INTERNAL-BOOK-GET
- **1 DEPTH**: 내부
- **2 DEPTH**: 소설
- **3 DEPTH**: 단건 조회
- **설명**: 타 서비스에서 특정 소설의 정보를 조회합니다.
- **사용처**: Reaction Service (댓글의 소설 정보 조회)
- **method**: `GET`
- **URL**: `/internal/books/{bookId}`
- **권한**: `System (내부 통신)`

☑ **Response Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| bookId | LONG | 소설 ID |
| title | STRING | 소설 제목 |
| writerId | LONG | 작성자 ID |
| status | STRING | 상태 (ONGOING/COMPLETED) |

---

#### 소설 정보 조회 (Batch)
- **API ID**: INTERNAL-BOOK-BATCH
- **1 DEPTH**: 내부
- **2 DEPTH**: 소설
- **3 DEPTH**: 일괄 조회
- **설명**: N+1 문제 방지를 위해 여러 소설의 정보를 한 번에 조회합니다.
- **사용처**: Reaction Service (댓글 목록의 소설 정보 조회)
- **method**: `GET`
- **URL**: `/internal/books/batch`
- **권한**: `System (내부 통신)`

☑ **Request Parameters (Query)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| bookIds | LIST<LONG> | O | 소설 ID 리스트 |

☑ **Response Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| books | MAP<LONG, BookInfoDto> | Key: bookId, Value: 소설 정보 |

---

#### 소설 존재 여부 확인
- **API ID**: INTERNAL-BOOK-EXISTS
- **1 DEPTH**: 내부
- **2 DEPTH**: 소설
- **3 DEPTH**: 존재 확인
- **설명**: 특정 소설이 존재하는지 확인합니다.
- **method**: `GET`
- **URL**: `/internal/books/{bookId}/exists`
- **권한**: `System (내부 통신)`

---

#### 문장으로 소설 ID 조회
- **API ID**: INTERNAL-SENTENCE-BOOK-ID
- **1 DEPTH**: 내부
- **2 DEPTH**: 문장
- **3 DEPTH**: 소설 ID 조회
- **설명**: 문장 ID로 해당 문장이 속한 소설의 ID를 조회합니다.
- **사용처**: Reaction Service (문장 투표 시 소설 ID 확인)
- **method**: `GET`
- **URL**: `/internal/sentences/{sentenceId}/book-id`
- **권한**: `System (내부 통신)`

☑ **Response**: `Long` (소설 ID)

---

#### 사용자별 소설 통계 조회
- **API ID**: INTERNAL-MEMBER-STORY-STATS
- **1 DEPTH**: 내부
- **2 DEPTH**: 통계
- **3 DEPTH**: 소설 통계
- **설명**: 특정 사용자가 작성한 소설 및 문장 수를 조회합니다.
- **사용처**: Member Service (마이페이지)
- **method**: `GET`
- **URL**: `/internal/members/{userId}/stats`
- **권한**: `System (내부 통신)`

☑ **Response Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| bookCount | INTEGER | 작성한 소설 수 |
| sentenceCount | INTEGER | 작성한 문장 수 |

---

#### 댓글 생성 알림 전송 (WebSocket Trigger)
- **API ID**: INTERNAL-NOTIFY-COMMENT
- **1 DEPTH**: 내부
- **2 DEPTH**: 알림
- **3 DEPTH**: 댓글 알림
- **설명**: Reaction Service에서 댓글 생성 시 호출하여 Story Service의 WebSocket을 통해 실시간 알림을 전송합니다.
- **사용처**: Reaction Service (댓글 작성 시)
- **method**: `POST`
- **URL**: `/internal/notify/comments`
- **권한**: `System (내부 통신)`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| commentId | LONG | O | 댓글 ID |
| bookId | LONG | O | 소설 ID |
| content | STRING | O | 댓글 내용 |
| nickname | STRING | O | 작성자 닉네임 |
| createdAt | DATETIME | O | 작성일시 |

☑ **WebSocket 브로드캐스트**: `/topic/comments/{bookId}`로 실시간 알림 전송

---

### 5-3. Reaction Service Internal API

#### 문장별 반응 정보 조회 (Batch)
- **API ID**: INTERNAL-SENTENCE-REACTIONS
- **1 DEPTH**: 내부
- **2 DEPTH**: 반응
- **3 DEPTH**: 문장 반응 조회
- **설명**: 여러 문장의 투표 정보를 한 번에 조회합니다.
- **사용처**: Story Service (소설 뷰어에서 각 문장의 투표 수 표시)
- **method**: `POST`
- **URL**: `/internal/reactions/sentences/stats`
- **권한**: `System (내부 통신)`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| sentenceIds | LIST<LONG> | O | 문장 ID 리스트 |

☑ **Request Parameters (Query)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userId | LONG | X | 현재 사용자 ID (투표 여부 확인용) |

☑ **Response Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| sentenceReactions | MAP<LONG, SentenceReactionInfoDto> | Key: sentenceId, Value: 반응 정보 |

**SentenceReactionInfoDto 구조:**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| likeCount | INTEGER | 좋아요 수 |
| dislikeCount | INTEGER | 싫어요 수 |
| userVoteType | STRING | 현재 사용자의 투표 (LIKE/DISLIKE/null) |

---

#### 소설 반응 정보 조회
- **API ID**: INTERNAL-BOOK-REACTIONS
- **1 DEPTH**: 내부
- **2 DEPTH**: 반응
- **3 DEPTH**: 소설 반응 조회
- **설명**: 특정 소설의 투표 및 댓글 수를 조회합니다.
- **사용처**: Story Service (소설 상세 정보 표시)
- **method**: `GET`
- **URL**: `/internal/reactions/books/{bookId}/stats`
- **권한**: `System (내부 통신)`

☑ **Request Parameters (Query)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userId | LONG | X | 현재 사용자 ID (투표 여부 확인용) |

☑ **Response Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| likeCount | INTEGER | 좋아요 수 |
| dislikeCount | INTEGER | 싫어요 수 |
| commentCount | INTEGER | 댓글 수 |
| userVoteType | STRING | 현재 사용자의 투표 (LIKE/DISLIKE/null) |

---

#### 사용자별 반응 통계 조회
- **API ID**: INTERNAL-MEMBER-REACTION-STATS
- **1 DEPTH**: 내부
- **2 DEPTH**: 통계
- **3 DEPTH**: 반응 통계
- **설명**: 특정 사용자가 작성한 댓글 수 및 투표 수를 조회합니다.
- **사용처**: Member Service (마이페이지)
- **method**: `GET`
- **URL**: `/internal/members/{userId}/stats`
- **권한**: `System (내부 통신)`

☑ **Response Parameter**
| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| commentCount | INTEGER | 작성한 댓글 수 |
| voteCount | INTEGER | 참여한 투표 수 |

---

## 🔐 6. 보안 및 인증

### 6-1. JWT 토큰 구조
- **Access Token**: 30분 유효, Authorization 헤더에 전송
- **Refresh Token**: 7일 유효, HttpOnly 쿠키에 저장
- **토큰 Payload**: userId, role, email 포함

### 6-2. Gateway 인증 흐름 및 헤더 명세
1. 클라이언트가 `Authorization: Bearer {token}` 헤더로 요청
2. Gateway가 JWT 검증 및 파싱
3. 토큰에서 추출한 정보를 HTTP 헤더로 변환:
   - `X-User-Id: {userId}` - 사용자 고유 ID (Long)
   - `X-User-Email: {email}` - 사용자 이메일
   - `X-User-Nickname: {nickname}` - 사용자 닉네임
   - `X-User-Role: {role}` - 사용자 역할 (USER/ADMIN)
4. 내부 서비스로 요청 전달
5. 각 서비스는 헤더 값만 읽어서 사용 (SecurityUtil 유틸리티 제공)

#### Gateway 주입 헤더 상세 명세

| 헤더명 | 타입 | 예시 | 설명 |
| --- | --- | --- | --- |
| X-User-Id | String (Long) | "1" | 사용자 고유 ID |
| X-User-Email | String | "user@example.com" | 사용자 이메일 |
| X-User-Nickname | String | "홍길동" | 사용자 닉네임 |
| X-User-Role | String | "USER" or "ADMIN" | 사용자 권한 |

**주의사항**:
- 이 헤더들은 Gateway에서만 주입되므로 직접 서비스 포트로 호출 시 인증이 적용되지 않습니다.
- 운영 환경에서는 내부 서비스 포트를 외부에 노출하지 않아야 합니다.
- Swagger 직접 테스트를 위해 JwtToHeaderFilter가 JWT를 헤더로 변환합니다.

### 6-3. 권한 레벨
- **비회원 (Public)**: 조회 API, 회원가입, 로그인
- **회원 (USER)**: 소설 생성, 문장 작성, 댓글 작성, 투표
- **관리자 (ADMIN)**: 모든 권한 + 회원 관리, 콘텐츠 관리
- **시스템 (System)**: Internal API (서비스 간 통신)

---

## 📊 7. 에러 응답 형식

모든 API는 통일된 에러 응답 형식을 사용합니다.

```json
{
  "code": "400",
  "message": "잘못된 요청입니다.",
  "data": null,
  "timestamp": "2026-01-16T10:30:00"
}
```

### 주요 에러 코드
| HTTP Status | Code | 설명 |
| --- | --- | --- |
| 400 | BAD_REQUEST | 잘못된 요청 (필수 파라미터 누락 등) |
| 401 | UNAUTHORIZED | 인증 실패 (토큰 없음/만료) |
| 403 | FORBIDDEN | 권한 없음 (작성자 아님, ADMIN 아님) |
| 404 | NOT_FOUND | 리소스 없음 (존재하지 않는 ID) |
| 409 | CONFLICT | 중복 (이미 투표함, 이미 존재하는 이메일) |
| 500 | INTERNAL_ERROR | 서버 오류 |
| 503 | SERVICE_UNAVAILABLE | 서비스 일시 중단 (Circuit Breaker Open) |

---

**Last Updated:** 2026-01-16
**Version:** 3.0

