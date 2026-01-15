package com.team2.storyservice.command.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문장 이어쓰기 요청 DTO
 * 릴레이 소설에 문장을 추가할 때 사용합니다.
 *
 * @author 정진호
 */
@Getter
@NoArgsConstructor
public class SentenceAppendRequest {

    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 1, max = 200, message = "문장은 1자 이상 200자 이하여야 합니다.")
    private String content;
}
