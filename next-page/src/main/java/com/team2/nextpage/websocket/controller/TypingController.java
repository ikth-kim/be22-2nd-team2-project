package com.team2.nextpage.websocket.controller;

import com.team2.nextpage.websocket.dto.TypingStatus;
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
     * 입력 시작 이벤트 처리
     * 클라이언트: /app/typing/start
     * 브로드캐스트: /topic/typing/{bookId}
     */
    @MessageMapping("/typing/start")
    public void handleTypingStart(TypingStatus status) {
        status.setTyping(true);
        log.debug("Typing started - Book: {}, User: {}", status.getBookId(), status.getUserNickname());
        messagingTemplate.convertAndSend("/topic/typing/" + status.getBookId(), status);
    }

    /**
     * 입력 종료 이벤트 처리
     * 클라이언트: /app/typing/stop
     * 브로드캐스트: /topic/typing/{bookId}
     */
    @MessageMapping("/typing/stop")
    public void handleTypingStop(TypingStatus status) {
        status.setTyping(false);
        log.debug("Typing stopped - Book: {}, User: {}", status.getBookId(), status.getUserNickname());
        messagingTemplate.convertAndSend("/topic/typing/" + status.getBookId(), status);
    }

    /**
     * 댓글 입력 시작 이벤트 처리
     * 클라이언트: /app/comment-typing/start
     * 브로드캐스트: /topic/comment-typing/{bookId}
     */
    @MessageMapping("/comment-typing/start")
    public void handleCommentTypingStart(TypingStatus status) {
        status.setTyping(true);
        log.debug("Comment typing started - Book: {}, User: {}", status.getBookId(), status.getUserNickname());
        messagingTemplate.convertAndSend("/topic/comment-typing/" + status.getBookId(), status);
    }

    /**
     * 댓글 입력 종료 이벤트 처리
     * 클라이언트: /app/comment-typing/stop
     * 브로드캐스트: /topic/comment-typing/{bookId}
     */
    @MessageMapping("/comment-typing/stop")
    public void handleCommentTypingStop(TypingStatus status) {
        status.setTyping(false);
        log.debug("Comment typing stopped - Book: {}, User: {}", status.getBookId(), status.getUserNickname());
        messagingTemplate.convertAndSend("/topic/comment-typing/" + status.getBookId(), status);
    }
}
