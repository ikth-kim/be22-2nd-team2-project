# 📋 Next Page API Specification
> **버전:** 2.2 (MSA Integrated)
> **최신 업데이트:** 2026-01-15
> **설명:** Next Page MSA 프로젝트의 전체 REST API 상세 명세서입니다.

---

## 🏗️ 1. 회원 (Member) 도메인

### 회원가입
- **API ID**: PGM-AUTH-001
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
| email | STRING | O | 사용자 이메일 (로그인 ID) |
| password | STRING | O | 비밀번호 |
| nickname | STRING | O | 사용자 닉네임 |

☑ **Response**: `String` ("회원가입 성공")

---

### 관리자 회원가입
- **API ID**: PGM-AUTH-ADMIN
- **1 DEPTH**: 회원
- **2 DEPTH**: 인증
- **3 DEPTH**: 관리자 가입
- **설명**: 신규 관리자 계정을 생성한다.
- **method**: `POST`
- **URL**: `/api/auth/admin`
- **권한**: `비회원`

☑ **Request Parameters (Body)**
| 파라미터명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| email | STRING | O | 관리자 이메일 |
| password | STRING | O | 비밀번호 |
| nickname | STRING | O | 관리자 닉네임 |

☑ **Response**: `String` ("관리자 가입 성공")

---

### 로그인
- **API ID**: PGM-AUTH-002
- **1 DEPTH**: 회원
- **2 DEPTH**: 인증
- **3 DEPTH**: 로그인
- **설명**: 이메일과 비밀번호로 로그인하여 JWT(Access/Refresh Token)를 발급받는다.
- **method**: `POST`
- **URL**: `/api/auth/login`
- **권한**: `모두`

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
- **권한**: `모두`

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
- **API ID**: PGM-ADMIN-WITHDRAW
- **1 DEPTH**: 회원
- **2 DEPTH**: 관리자 기능
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
- **Request (Query)**: `email`

---

### 닉네임 중복 체크
- **API ID**: PGM-CHECK-NICK
- **1 DEPTH**: 회원
- **2 DEPTH**: 검증
- **3 DEPTH**: 닉네임 체크
- **설명**: 닉네임 중복 여부를 확인한다.
- **method**: `GET`
- **URL**: `/api/auth/check-nickname`
- **Request (Query)**: `nickname`

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
| list | ARRAY | 활동 내역 등 |

---

## 📕 2. 소설 (Story) 도메인

### 카테고리 목록 조회
- **API ID**: PGM-CAT-LIST
- **설명**: 소설 카테고리 전체 목록을 조회한다.
- **method**: `GET`
- **URL**: `/api/categories`

---

### 소설 생성
- **API ID**: PGM-BOOK-CREATE
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

### 소설 목록 조회 (검색/필터)
- **API ID**: PGM-BOOK-LIST
- **설명**: 조건에 따라 소설 목록을 페이징하여 조회한다.
- **method**: `GET`
- **URL**: `/api/books`
- **Request (Query)**: `page`, `size`, `sortBy`, `keyword`, `categoryId`

---

### 소설 상세 조회 (기본)
- **API ID**: PGM-BOOK-DETAIL
- **설명**: 소설의 기본 정보를 조회한다.
- **method**: `GET`
- **URL**: `/api/books/{bookId}`

---

### 소설 뷰어 조회 (문장 포함)
- **API ID**: PGM-BOOK-VIEW
- **설명**: 소설의 전체 내용(문장 리스트 포함)을 뷰어 모드로 조회한다.
- **method**: `GET`
- **URL**: `/api/books/{bookId}/view`

---

### 문장 이어쓰기
- **API ID**: PGM-BOOK-APPEND
- **설명**: 진행 중인 소설에 새로운 문장을 추가한다.
- **method**: `POST`
- **URL**: `/api/books/{bookId}/sentences`
- **권한**: `회원`, `관리자`
- **Request (Body)**: `content` (이어쓸 문장 내용)

---

### 소설 제목 수정
- **API ID**: PGM-BOOK-UPDATE
- **설명**: 소설 제목을 수정한다. (작성자/관리자)
- **method**: `PATCH`
- **URL**: `/api/books/{bookId}`
- **Request (Body)**: `title`

---

### 문장 수정
- **API ID**: PGM-SENTENCE-UPDATE
- **설명**: 특정 문장의 내용을 수정한다. (작성자/관리자)
- **method**: `PATCH`
- **URL**: `/api/books/{bookId}/sentences/{sentenceId}`
- **Request (Body)**: `content`

---

### 소설 삭제
- **API ID**: PGM-BOOK-DELETE
- **설명**: 소설을 삭제한다. (작성자/관리자)
- **method**: `DELETE`
- **URL**: `/api/books/{bookId}`

---

### 문장 삭제
- **API ID**: PGM-SENTENCE-DELETE
- **설명**: 특정 문장을 삭제한다. 순서가 자동 재정렬된다. (작성자/관리자)
- **method**: `DELETE`
- **URL**: `/api/books/{bookId}/sentences/{sentenceId}`

---

### 소설 수동 완결
- **API ID**: PGM-BOOK-COMPLETE
- **설명**: 작성자가 소설을 수동으로 완결 처리한다.
- **method**: `POST`
- **URL**: `/api/books/{bookId}/complete`

---

### 내가 쓴 문장 조회
- **API ID**: PGM-MY-SENTENCES
- **설명**: 로그인한 사용자가 작성한 문장들을 조회한다.
- **method**: `GET`
- **URL**: `/api/books/mysentences`
- **Request (Query)**: `page`, `size`

---

## 💬 3. 반응 (Reaction) 도메인

### 댓글 작성
- **API ID**: PGM-COMMENT-CREATE
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
- **설명**: 댓글 내용을 수정한다.
- **method**: `PATCH`
- **URL**: `/api/reactions/comments/{commentId}`
- **Request (Body)**: `content`

---

### 댓글 삭제
- **API ID**: PGM-COMMENT-DELETE
- **설명**: 댓글을 삭제한다.
- **method**: `DELETE`
- **URL**: `/api/reactions/comments/{commentId}`

---

### 댓글 목록 조회
- **API ID**: PGM-COMMENT-LIST
- **설명**: 특정 소설의 댓글 목록을 조회한다.
- **method**: `GET`
- **URL**: `/api/reactions/comments/{bookId}`

---

### 내가 쓴 댓글 조회
- **API ID**: PGM-MY-COMMENTS
- **설명**: 내가 작성한 댓글 목록을 페이징하여 조회한다.
- **method**: `GET`
- **URL**: `/api/reactions/mycomments`

---

### 소설 투표 (좋아요/싫어요)
- **API ID**: PGM-VOTE-BOOK
- **설명**: 소설에 대해 투표를 하거나 취소한다.
- **method**: `POST`
- **URL**: `/api/reactions/votes/books`
- **Body**: `bookId`, `voteType` (LIKE/DISLIKE)

---

### 문장 투표 (좋아요/싫어요)
- **API ID**: PGM-VOTE-SENTENCE
- **설명**: 특정 문장에 대해 투표를 하거나 취소한다.
- **method**: `POST`
- **URL**: `/api/reactions/votes/sentences/{sentenceId}`
- **Body**: `voteType` (LIKE/DISLIKE)
