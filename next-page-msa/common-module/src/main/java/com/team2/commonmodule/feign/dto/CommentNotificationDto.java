package com.team2.commonmodule.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentNotificationDto {
    private Long commentId;
    private Long bookId;
    private String content;
    private String nickname;
    private LocalDateTime createdAt;
}
