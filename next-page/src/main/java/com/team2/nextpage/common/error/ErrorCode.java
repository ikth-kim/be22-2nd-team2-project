package com.team2.nextpage.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 전역적으로 사용되는 에러 코드를 정의한 Enum입니다.
 * 비즈니스 로직에서 발생하는 모든 예외는 이 Enum을 통해 관리됩니다.
 *
 * @author 정진호
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "요청하신 리소스를 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),

    // Security & Auth (김태형님 도메인이지만 공통 에러로 정의)
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A001", "이메일 또는 비밀번호가 일치하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "A003", "로그인이 필요합니다."),

    // Book & Story (정진호 Core Logic)
    NOT_YOUR_TURN(HttpStatus.BAD_REQUEST, "B001", "아직 당신의 순서가 아닙니다."),
    ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "B002", "이미 완결된 소설입니다."),
    CONSECUTIVE_WRITING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "B003", "연속해서 글을 쓸 수 없습니다."),
    SEQUENCE_MISMATCH(HttpStatus.BAD_REQUEST, "B004", "문장 순서가 일치하지 않습니다."),
    NOT_BOOK_OWNER(HttpStatus.FORBIDDEN, "B005", "소설 작성자만 수행할 수 있습니다."),

    // Member (김태형 Member Domain)
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M001", "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "M002", "이미 사용 중인 닉네임입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M003", "회원 정보를 찾을 수 없습니다."),

    // Reaction (정병진 Reaction Domain)
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "존재하지 않는 댓글입니다."),
    NOT_COMMENT_OWNER(HttpStatus.FORBIDDEN, "R002", "작성자만 수정/삭제할 수 있습니다."),
    EMPTY_CONTENT(HttpStatus.BAD_REQUEST, "R003", "내용을 입력해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
