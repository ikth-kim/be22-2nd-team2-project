# 📋 Next Page API Specification
> **버전:** 2.3 (MSA Complete)
> **최신 업데이트:** 2026-01-15
> **설명:** Next Page MSA 프로젝트의 전체 REST API 및 WebSocket 명세서입니다.

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
- **Subscribe**: `/topic/books/{bookId}/sentences`

---

## 📌 5. 내부 API (Internal API - Feign)

### 회원 정보 조회 (단건)
- **ID**: INTERNAL-MEMBER-GET
- **1 DEPTH**: 내부
- **2 DEPTH**: 회원
- **3 DEPTH**: 단건 조회
- **설명**: 타 서비스에서 회원 정보를 조회한다.
- **method**: `GET`
- **URL**: `/internal/members/{memberId}`
- **권한**: `System`

### 회원 정보 조회 (Batch)
- **ID**: INTERNAL-MEMBER-BATCH
- **1 DEPTH**: 내부
- **2 DEPTH**: 회원
- **3 DEPTH**: 일괄 조회
- **설명**: N+1 방지를 위해 여러 회원의 정보를 한 번에 조회한다.
- **method**: `POST`
- **URL**: `/internal/members/batch`
- **권한**: `System`

