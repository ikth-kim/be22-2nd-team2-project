package com.team2.reactionservice.query.reaction.service;

import com.team2.commonmodule.feign.MemberServiceClient;
import com.team2.commonmodule.feign.StoryServiceClient;
import com.team2.commonmodule.feign.dto.BookBatchInfoDto;
import com.team2.commonmodule.feign.dto.BookInfoDto;
import com.team2.commonmodule.feign.dto.MemberBatchInfoDto;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.reactionservice.query.reaction.dto.response.CommentDto;
import com.team2.reactionservice.query.reaction.dto.response.CommentPageResponse;
import com.team2.reactionservice.query.reaction.mapper.ReactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 반응 Query 서비스 (댓글 목록 조회 등)
 *
 * @author 정병진
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReactionQueryService {

  private final ReactionMapper reactionMapper;
  private final MemberServiceClient memberServiceClient;
  private final StoryServiceClient storyServiceClient;

  /**
   * 댓글 목록 조회
   */
  public List<CommentDto> getComments(Long bookId) {
    // 1. 전체 댓글 조회 (Soft Delete 제외됨)
    List<CommentDto> allComments = reactionMapper.findCommentsByBookId(bookId);

    // 2. MSA: 회원 정보 조회 (Feign Client)
    List<Long> writerIds = allComments.stream()
            .map(CommentDto::getWriterId)
            .distinct()
            .collect(Collectors.toList());

    if (!writerIds.isEmpty()) {
      try {
        ApiResponse<MemberBatchInfoDto> response = memberServiceClient.getMembersBatch(writerIds);
        if (response != null && response.getData() != null) {
          Map<Long, String> memberMap = response.getData().getMembers().stream()
                  .collect(Collectors.toMap(
                          MemberInfoDto::getUserId,
                          MemberInfoDto::getUserNicknm
                  ));

          // 각 댓글 작성자 닉네임 설정
          allComments.forEach(comment ->
                  comment.setWriterNicknm(memberMap.get(comment.getWriterId()))
          );
        }
      } catch (Exception e) {
        log.warn("Failed to fetch member info from member-service: {}", e.getMessage());
      }
    }

    // 3. Map으로 변환 (Lookup 용도)
    Map<Long, CommentDto> commentMap = allComments.stream()
        .collect(Collectors.toMap(CommentDto::getCommentId, dto -> dto));

    // 4. 트리 구조 조립
    List<CommentDto> roots = new java.util.ArrayList<>();

    for (CommentDto dto : allComments) {
      if (dto.getParentId() == null) {
        roots.add(dto);
      } else {
        CommentDto parent = commentMap.get(dto.getParentId());
        if (parent != null) {
          parent.getChildren().add(dto);
        } else {
          // 부모가 삭제되었거나 찾을 수 없는 경우, 최상위로 노출 (Orphan 처리)
          roots.add(dto);
        }
      }
    }

    // 5. 최상위 댓글만 반환 (자식은 children 필드에 포함됨)
    return roots;
  }

  /**
   * 특정 사용자가 쓴 댓글 목록 조회 (페이징)
   */
  public CommentPageResponse getCommentsByUser(Long userId, int page, int size) {
    int offset = page * size;
    List<CommentDto> comments = reactionMapper.findCommentsByWriterId(userId, offset, size);
    Long totalElements = reactionMapper.countCommentsByWriterId(userId);

    if (!comments.isEmpty()) {
      // MSA: 회원 정보 조회 (Feign Client)
      try {
        ApiResponse<MemberInfoDto> memberResponse = memberServiceClient.getMemberInfo(userId);
        if (memberResponse != null && memberResponse.getData() != null) {
          String nicknm = memberResponse.getData().getUserNicknm();
          comments.forEach(comment -> comment.setWriterNicknm(nicknm));
        }
      } catch (Exception e) {
        log.warn("Failed to fetch member info from member-service: {}", e.getMessage());
      }

      // MSA: 소설 정보 조회 (Feign Client)
      List<Long> bookIds = comments.stream()
              .map(CommentDto::getBookId)
              .distinct()
              .collect(Collectors.toList());

      if (!bookIds.isEmpty()) {
        try {
          ApiResponse<BookBatchInfoDto> bookResponse = storyServiceClient.getBooksBatch(bookIds);
          if (bookResponse != null && bookResponse.getData() != null) {
            Map<Long, String> bookMap = bookResponse.getData().getBooks().stream()
                    .collect(Collectors.toMap(
                            BookInfoDto::getBookId,
                            BookInfoDto::getTitle
                    ));

            // 각 댓글의 소설 제목 설정
            comments.forEach(comment ->
                    comment.setBookTitle(bookMap.get(comment.getBookId()))
            );
          }
        } catch (Exception e) {
          log.warn("Failed to fetch book info from story-service: {}", e.getMessage());
        }
      }
    }

    return new CommentPageResponse(comments, page, size, totalElements);
  }
}
