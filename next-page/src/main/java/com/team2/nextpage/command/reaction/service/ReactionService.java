package com.team2.nextpage.command.reaction.service;

import com.team2.nextpage.command.reaction.dto.request.CreateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.nextpage.command.reaction.entity.Comment;
import com.team2.nextpage.command.reaction.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 반응(댓글/투표) Command 서비스
 *
 * @author 정병진
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReactionService {

  private final CommentRepository commentRepository;

  /**
   * 댓글 작성
   *
   * @param request 댓글 작성 요청 정보(bookId, content)
   * @return        생성된 댓글의 ID
   */
  public Long addComment(CreateCommentRequest request) {

    long writerId = 1L; // 추후 회원 및 인증 기능 완성 시 'userId'로 값 변경

    Comment newComment = Comment.builder()
        .bookId(request.getBookId())
        .writerId(writerId)
        .content(request.getContent())
        .build();

    Comment saveComment = commentRepository.save(newComment);

    return saveComment.getCommentId();
  }

  /**
   * 댓글 수정
   *
   * @param commentId 수정할 댓글 ID
   * @param request   수정할 내용이 담긴 DTO
   */
  public void modifyComment(Long commentId, UpdateCommentRequest request) {

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

    // 권한 체크 (타인 댓글 수정/삭제 불가 예외 처리)
    validateWriter(comment, 1L); // 추후 회원 및 인증 기능 완성 시 'userId'로 값 변경

    comment.updateContent(request.getContent());
  }

  /**
   * 댓글 삭제
   *
   * @param commentId 삭제할 댓글 ID
   */
  public void removeComment(Long commentId){
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

    // 권한 체크 (타인 댓글 수장/삭제 불가 예외 처리)
    validateWriter(comment, 1L); // 추후 회원 및 인증 기능 완성 시 'userId'로 값 변경

    commentRepository.delete(comment);


  }

  /**
   * 소설 좋아요 투표
   * <p>
   * [Hint]
   * 1. VoteRequest(bookId, voteType)를 인자로 받습니다.
   * 2. 이미 투표했는지 중복 확인을 수행합니다.
   * 3. BookVote Entity를 저장합니다.
   * 4. 투표 결과를 반환합니다 (true: 투표 성공, false: 취소 등).
   */
  public Boolean voteBook(/* VoteRequest request */) {
    // TODO: 정병진 구현 필요

    return null;
  }

  /**
   * 작성자 권한 검증(내부 헬퍼 메서드)
   * 요청한 사용자(userId)가 댓글 작성자(writerId)와 일치하는지 확인합니다.
   *
   * @param comment 검증할 댓글 엔티티
   * @param userId  요청을 보낸 사용자의 ID
   * @throws IllegalArgumentException 작성자가 아닐 경우 예외 발생
   */
  private void validateWriter(Comment comment, Long userId) {
    if (!comment.getWriterId().equals(userId)){
      throw new IllegalArgumentException("작성자만 수정/삭제할 수 있습니다.");
    }
  }
}
