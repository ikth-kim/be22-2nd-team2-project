package com.team2.nextpage.command.reaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 댓글 수정 요청 DTO
 *
 * @author 정병진
 */
@Getter
@NoArgsConstructor
public class UpdateCommentRequest {

  @NotBlank(message = "수정할 내용을 입력해주세요.")
  @Size(max = 500, message = "댓글은 500자 이내로 작성해주세요.")
  private String content;

}
