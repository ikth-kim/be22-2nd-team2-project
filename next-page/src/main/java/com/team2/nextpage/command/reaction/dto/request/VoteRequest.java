package com.team2.nextpage.command.reaction.dto.request;

import com.team2.nextpage.command.reaction.entity.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 투표 요청 DTO
 *
 * @author 정병진
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VoteRequest {

  private Long bookId; // 소설 투표 시 사용

  @NotNull(message = "투표 유형을 선택해주세요.")
  private VoteType voteType;
}
