package com.team2.nextpage.command.reaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 작성 요청 DTO
 *
 * @author 정병진
 */
@Getter
@NoArgsConstructor
public class CreateCommentRequest {

    @NotNull(message = "소설 ID를 입력해주세요(필수)")
    private Long bookId;

    @NotBlank(message = "댓글을 입력해주세요, 내용은 비어있을 수 없습니다.")
    @Size(max = 500, message = "댓글은 500자 이내로 작성 해주세요.")
    private String content;
}
