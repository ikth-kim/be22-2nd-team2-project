package com.team2.nextpage.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 입력 상태 메시지 DTO
 *
 * @author 정진호
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TypingStatus {

    private Long bookId;
    private String userNickname;
    private boolean isTyping;

    public static TypingStatus startTyping(Long bookId, String userNickname) {
        return new TypingStatus(bookId, userNickname, true);
    }

    public static TypingStatus stopTyping(Long bookId, String userNickname) {
        return new TypingStatus(bookId, userNickname, false);
    }
}
