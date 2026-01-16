package com.team2.storyservice.websocket.controller;

import com.team2.storyservice.websocket.dto.TypingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket 메시지 처리 컨트롤러
 * 실시간 입력 상태 브로드캐스트
 *
 * @author 정진호
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class TypingController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 입력 상태 처리 (sentences & comments)
     * 클라이언트: /app/typing/{bookId} OR /app/comment-typing/{bookId}
     * 브로드캐스트: /topic/typing/{bookId} OR /topic/comment-typing/{bookId}
     */
    @MessageMapping("/typing/{bookId}")
    public void handleTyping(@org.springframework.messaging.handler.annotation.DestinationVariable Long bookId,
            TypingStatus status) {
        status.setBookId(bookId); // Path Variable에서 ID 설정
        log.debug("Typing status - Book: {}, User: {}, Typing: {}", bookId, status.getUserNickname(),
                status.isTyping());
        messagingTemplate.convertAndSend("/topic/typing/" + bookId, status);
    }

    @MessageMapping("/comment-typing/{bookId}")
    public void handleCommentTyping(@org.springframework.messaging.handler.annotation.DestinationVariable Long bookId,
            TypingStatus status) {
        status.setBookId(bookId);
        log.debug("Comment typing status - Book: {}, User: {}, Typing: {}", bookId, status.getUserNickname(),
                status.isTyping());
        messagingTemplate.convertAndSend("/topic/comment-typing/" + bookId, status);
    }
}
