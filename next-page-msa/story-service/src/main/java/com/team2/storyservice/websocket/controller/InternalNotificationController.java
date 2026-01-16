package com.team2.storyservice.websocket.controller;

import com.team2.commonmodule.feign.dto.CommentNotificationDto;
import com.team2.commonmodule.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 내부 서비스 간 알림 처리를 위한 컨트롤러
 *
 * @author 정진호
 */
@Slf4j
@RestController
@RequestMapping("/internal/notify")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 댓글 생성 알림 처리 (From Reaction Service)
     * WebSocket을 통해 해당 소설 구독자들에게 댓글 알림을 전송합니다.
     */
    @PostMapping("/comments")
    public ApiResponse<Void> notifyCommentCreated(@RequestBody CommentNotificationDto notificationDto) {
        log.info("Received comment notification for book: {}", notificationDto.getBookId());

        // Broadcast to WebSocket subscribers
        messagingTemplate.convertAndSend("/topic/comments/" + notificationDto.getBookId(), notificationDto);

        return ApiResponse.success();
    }
}
