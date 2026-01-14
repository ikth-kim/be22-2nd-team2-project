package com.team2.nextpage.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteUpdateDto {
    private String targetType; // "BOOK" or "SENTENCE"
    private Long targetId;
    private long likeCount;
    private long dislikeCount;
}
