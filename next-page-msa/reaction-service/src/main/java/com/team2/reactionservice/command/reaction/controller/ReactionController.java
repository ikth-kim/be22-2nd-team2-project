package com.team2.reactionservice.command.reaction.controller;

import com.team2.reactionservice.command.reaction.dto.request.CreateCommentRequest;
import com.team2.reactionservice.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.reactionservice.command.reaction.dto.request.VoteRequest;
import com.team2.reactionservice.command.reaction.service.ReactionService;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.commonmodule.util.SecurityUtil;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.team2.commonmodule.feign.MemberServiceClient;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import java.time.LocalDateTime;
import com.team2.commonmodule.feign.StoryServiceClient;
import lombok.RequiredArgsConstructor;

@Tag(name = "Reaction Commands", description = "반응(댓글/투표) 관리(Command) API")
@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

  private final ReactionService reactionService;
  private final MemberServiceClient memberServiceClient;
  private final StoryServiceClient storyServiceClient;

  /**
   * 댓글 등록 API
   * POST /api/reactions/comments
   *
   * @param request 댓글 작성 요청 객체 (Body)
   * @return 생성된 댓글의 ID
   */
  @Operation(summary = "댓글 작성", description = "소설에 새로운 댓글을 작성합니다.")
  @PostMapping("/comments")
  public ResponseEntity<ApiResponse<Long>> createComment(
      @RequestBody @Valid CreateCommentRequest request) {
    Long commentId = reactionService.addComment(request);

    // 실시간 댓글 알림 전송 (via Story Service WebSocket)
    Long userId = SecurityUtil.getCurrentUserId();
    String nickname = "User#" + userId;
    try {
      ApiResponse<MemberInfoDto> response = memberServiceClient.getMemberInfo(userId);
      if (response != null && response.getData() != null) {
        nickname = response.getData().getUserNicknm();
      }
    } catch (Exception e) {
      // Fallback or log error
    }

    com.team2.commonmodule.feign.dto.CommentNotificationDto notificationDto = com.team2.commonmodule.feign.dto.CommentNotificationDto
        .builder()
        .commentId(commentId)
        .bookId(request.getBookId())
        .content(request.getContent())
        .nickname(nickname)
        .createdAt(LocalDateTime.now())
        .build();

    try {
      storyServiceClient.notifyCommentCreated(notificationDto);
    } catch (Exception e) {
      // WebSocket 알림 실패가 댓글 생성 실패로 이어지면 안 됨
      // log.error("Failed to notify story service about new comment", e);
    }

    return ResponseEntity.ok(ApiResponse.success(commentId));
  }

  /**
   * 댓글 수정 API
   * PATCH /api/reactions/comments/{commentId}
   *
   * @param commentId 수정할 댓글의 ID
   * @param request   수정할 내용이 담긴 요청 객체(Body)
   * @return 성공시 데이터 없이 성공 응답 반환
   */
  @Operation(summary = "댓글 수정", description = "작성한 댓글을 수정합니다.")
  @PatchMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> modifyComment(
      @PathVariable Long commentId,
      @RequestBody @Valid UpdateCommentRequest request) {
    reactionService.modifyComment(commentId, request);
    return ResponseEntity.ok(ApiResponse.success());
  }

  /**
   * 댓글 삭제 API
   * DELETE /api/reactions/comments/{commentId}
   *
   * @param commentId 삭제할 댓글의 ID (Path Variable)
   * @return 성공 시 데이터 없이 성공 응답 반환
   */
  @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다.")
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> removeComment(
      @PathVariable Long commentId) {
    reactionService.removeComment(commentId);
    return ResponseEntity.ok(ApiResponse.success());
  }

  /**
   * 소설 투표 API
   * POST /api/reactions/votes/books
   *
   * @param request 투표 요청 정보 (bookId, voteType)
   * @return 투표 반영 여부 (true: 반영됨/변경됨 / false: 취소됨)
   */
  @Operation(summary = "소설 투표", description = "소설에 대해 좋아요/싫어요를 투표합니다.\n" +
      "- **최초 투표**: 투표가 **반영**되며 `true`를 반환합니다.\n" +
      "- **다른 타입으로 변경**: 기존 투표가 **변경**되며 `true`를 반환합니다. (예: 좋아요 -> 싫어요)\n" +
      "- **동일 타입 재투표**: 기존 투표가 **취소**되며 `false`를 반환합니다. (예: 좋아요 -> 좋아요)")
  @PostMapping("/votes/books")
  public ResponseEntity<ApiResponse<Boolean>> voteBook(
      @RequestBody @Valid VoteRequest request) {
    Boolean result = reactionService.voteBook(request);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  /**
   * 문장 투표 API
   * POST /api/reactions/votes/sentences/{sentenceId}
   *
   * @param sentenceId 투표할 문장 ID
   * @param request    투표 요청 정보 (voteType)
   * @return 투표 반영 여부 (true: 반영됨/변경됨 / false: 취소됨)
   */
  @Operation(summary = "문장 투표", description = "특정 문장에 대해 좋아요/싫어요를 투표합니다.\n" +
      "- **최초 투표**: 투표가 **반영**되며 `true`를 반환합니다.\n" +
      "- **다른 타입으로 변경**: 기존 투표가 **변경**되며 `true`를 반환합니다.\n" +
      "- **동일 타입 재투표**: 기존 투표가 **취소**되며 `false`를 반환합니다.")
  @PostMapping("/votes/sentences/{sentenceId}")
  public ResponseEntity<ApiResponse<Boolean>> voteSentence(
      @PathVariable Long sentenceId,
      @RequestBody @Valid VoteRequest request) {
    Boolean result = reactionService.voteSentence(sentenceId, request);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
