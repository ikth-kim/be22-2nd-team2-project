package com.team2.nextpage.command.reaction.service;

import com.team2.nextpage.command.book.entity.Book;
import com.team2.nextpage.command.book.entity.Sentence;
import com.team2.nextpage.command.book.repository.SentenceRepository;
import com.team2.nextpage.command.reaction.dto.request.CreateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.VoteRequest;
import com.team2.nextpage.command.reaction.entity.*;
import com.team2.nextpage.command.reaction.repository.BookVoteRepository;
import com.team2.nextpage.command.reaction.repository.CommentRepository;
import com.team2.nextpage.command.reaction.repository.SentenceVoteRepository;
import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.fixtures.*;
import com.team2.nextpage.websocket.dto.VoteUpdateDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    @InjectMocks
    private ReactionService reactionService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookVoteRepository bookVoteRepository;

    @Mock
    private SentenceVoteRepository sentenceVoteRepository;

    @Mock
    private SentenceRepository sentenceRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private static MockedStatic<SecurityUtil> securityUtil;

    @BeforeAll
    public static void beforeAll() {
        securityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterAll
    public static void afterAll() {
        securityUtil.close();
    }

    @Test
    @DisplayName("댓글 작성 성공 - 루트 댓글")
    void addComment_Success_RootComment() {
        // given
        Long userId = 1L;
        Long bookId = 10L;
        CreateCommentRequest request = RequestDtoTestBuilder.commentRequest(bookId, "Test Comment");

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);

        Comment savedComment = CommentTestBuilder.aComment()
                .withCommentId(100L)
                .withBookId(bookId)
                .withWriterId(userId)
                .withContent("Test Comment")
                .build();
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        Long commentId = reactionService.addComment(request);

        // then
        assertThat(commentId).isEqualTo(100L);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 본인")
    void removeComment_Success_Owner() {
        // given
        Long userId = 1L;
        Long commentId = 100L;
        Comment comment = CommentTestBuilder.aComment()
                .withCommentId(commentId)
                .withWriterId(userId)
                .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when
        reactionService.removeComment(commentId);

        // then
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    void removeComment_Fail_NotOwner() {
        // given
        Long userId = 1L;
        Long otherUserId = 2L;
        Long commentId = 100L;
        Comment comment = Comment.builder()
                .writerId(otherUserId) // 작성자 다름
                .build();
        ReflectionTestUtils.setField(comment, "commentId", commentId);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reactionService.removeComment(commentId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_OWNER);
    }

    @Test
    @DisplayName("소설 투표 성공 - 신규 투표")
    void voteBook_NewVote() {
        // given
        Long userId = 1L;
        VoteRequest request = VoteRequest.builder()
                .bookId(10L)
                .voteType(VoteType.LIKE)
                .build();

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(bookVoteRepository.findByBookIdAndVoterId(10L, userId)).willReturn(Optional.empty());

        // when
        Boolean result = reactionService.voteBook(request);

        // then
        assertThat(result).isTrue();
        verify(bookVoteRepository, times(1)).save(any(BookVote.class));
    }

    @Test
    @DisplayName("문장 투표 성공 - 신규 투표")
    void voteSentence_NewVote() {
        // given
        Long userId = 1L;
        Long sentenceId = 50L;
        VoteRequest request = VoteRequest.builder()
                .voteType(VoteType.LIKE)
                .build();

        Book book = Book.builder().bookId(10L).build();
        Sentence sentence = Sentence.builder().sentenceId(sentenceId).book(book).build();

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(sentenceVoteRepository.findBySentenceIdAndVoterId(sentenceId, userId)).willReturn(Optional.empty());
        given(sentenceRepository.findById(sentenceId)).willReturn(Optional.of(sentence));

        // when
        Boolean result = reactionService.voteSentence(sentenceId, request);

        // then
        assertThat(result).isTrue();
        verify(sentenceVoteRepository, times(1)).save(any(SentenceVote.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("댓글 수정 성공 - 본인")
    void modifyComment_Success_Owner() {
        // given
        Long userId = 1L;
        Long commentId = 100L;
        Comment comment = Comment.builder().writerId(userId).build();
        ReflectionTestUtils.setField(comment, "commentId", commentId);
        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("Updated Content")
                .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when
        reactionService.modifyComment(commentId, request);

        // then
        assertThat(comment.getContent()).isEqualTo("Updated Content");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 권한 없음")
    void modifyComment_Fail_NotOwner() {
        // given
        Long userId = 1L;
        Long otherUserId = 2L;
        Long commentId = 100L;
        Comment comment = Comment.builder().writerId(otherUserId).build();
        ReflectionTestUtils.setField(comment, "commentId", commentId);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(SecurityUtil.isAdmin()).willReturn(false);

        // when & then
        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("Unauthorized Update")
                .build();
        assertThatThrownBy(() -> reactionService.modifyComment(commentId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_OWNER);
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 관리자")
    void removeComment_Success_Admin() {
        // given
        Long adminId = 99L;
        Long commentId = 100L;
        Comment comment = Comment.builder().writerId(1L).build();
        ReflectionTestUtils.setField(comment, "commentId", commentId);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(SecurityUtil.getCurrentUserId()).willReturn(adminId);
        given(SecurityUtil.isAdmin()).willReturn(true);

        // when
        reactionService.removeComment(commentId);

        // then
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    @DisplayName("소설 투표 취소 - 기존 투표와 동일 타입")
    void voteBook_CancelSameType() {
        // given
        Long userId = 1L;
        Long bookId = 10L;
        VoteRequest request = RequestDtoTestBuilder.likeRequest(bookId);
        BookVote existingVote = VoteTestBuilder.BookVoteBuilder.aBookVote()
                .withBookId(bookId)
                .withVoterId(userId)
                .asLike()
                .build();

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(bookVoteRepository.findByBookIdAndVoterId(bookId, userId)).willReturn(Optional.of(existingVote));
        given(bookVoteRepository.countByBookIdAndVoteType(anyLong(), any())).willReturn(0L);

        // when
        Boolean result = reactionService.voteBook(request);

        // then
        assertThat(result).isFalse();
        verify(bookVoteRepository, times(1)).delete(existingVote);
        verify(bookVoteRepository, never()).save(any(BookVote.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(VoteUpdateDto.class));
    }

    @Test
    @DisplayName("소설 투표 변경 - LIKE → DISLIKE")
    void voteBook_ChangeType_LikeToDislike() {
        // given
        Long userId = 1L;
        Long bookId = 10L;
        VoteRequest request = RequestDtoTestBuilder.dislikeRequest(bookId);
        BookVote existingVote = VoteTestBuilder.BookVoteBuilder.aBookVote()
                .withBookId(bookId)
                .withVoterId(userId)
                .asLike()
                .build();

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(bookVoteRepository.findByBookIdAndVoterId(bookId, userId)).willReturn(Optional.of(existingVote));
        given(bookVoteRepository.countByBookIdAndVoteType(anyLong(), any())).willReturn(0L);

        // when
        Boolean result = reactionService.voteBook(request);

        // then
        assertThat(result).isTrue();
        assertThat(existingVote.getVoteType()).isEqualTo(VoteType.DISLIKE);
        verify(bookVoteRepository, never()).delete(any());
        verify(bookVoteRepository, never()).save(any());
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(VoteUpdateDto.class));
    }

    @Test
    @DisplayName("문장 투표 취소 - 기존 투표와 동일 타입")
    void voteSentence_CancelSameType() {
        // given
        Long userId = 1L;
        Long sentenceId = 50L;
        Long bookId = 10L;
        VoteRequest request = RequestDtoTestBuilder.voteRequest(null, VoteType.LIKE);

        Book book = BookTestBuilder.aBook().withBookId(bookId).build();
        Sentence sentence = SentenceTestBuilder.aSentence()
                .withSentenceId(sentenceId)
                .withBook(book)
                .build();

        SentenceVote existingVote = VoteTestBuilder.SentenceVoteBuilder.aSentenceVote()
                .withSentenceId(sentenceId)
                .withVoterId(userId)
                .asLike()
                .build();

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(sentenceVoteRepository.findBySentenceIdAndVoterId(sentenceId, userId))
                .willReturn(Optional.of(existingVote));
        given(sentenceRepository.findById(sentenceId)).willReturn(Optional.of(sentence));
        given(sentenceVoteRepository.countBySentenceIdAndVoteType(anyLong(), any())).willReturn(0L);

        // when
        Boolean result = reactionService.voteSentence(sentenceId, request);

        // then
        assertThat(result).isFalse();
        verify(sentenceVoteRepository, times(1)).delete(existingVote);
        verify(sentenceVoteRepository, never()).save(any());
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(VoteUpdateDto.class));
    }

    @Test
    @DisplayName("문장 투표 변경 - DISLIKE → LIKE")
    void voteSentence_ChangeType_DislikeToLike() {
        // given
        Long userId = 1L;
        Long sentenceId = 50L;
        Long bookId = 10L;
        VoteRequest request = RequestDtoTestBuilder.voteRequest(null, VoteType.LIKE);

        Book book = BookTestBuilder.aBook().withBookId(bookId).build();
        Sentence sentence = SentenceTestBuilder.aSentence()
                .withSentenceId(sentenceId)
                .withBook(book)
                .build();

        SentenceVote existingVote = VoteTestBuilder.SentenceVoteBuilder.aSentenceVote()
                .withSentenceId(sentenceId)
                .withVoterId(userId)
                .asDislike()
                .build();

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(sentenceVoteRepository.findBySentenceIdAndVoterId(sentenceId, userId))
                .willReturn(Optional.of(existingVote));
        given(sentenceRepository.findById(sentenceId)).willReturn(Optional.of(sentence));
        given(sentenceVoteRepository.countBySentenceIdAndVoteType(anyLong(), any())).willReturn(0L);

        // when
        Boolean result = reactionService.voteSentence(sentenceId, request);

        // then
        assertThat(result).isTrue();
        assertThat(existingVote.getVoteType()).isEqualTo(VoteType.LIKE);
        verify(sentenceVoteRepository, never()).delete(any());
        verify(sentenceVoteRepository, never()).save(any());
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(VoteUpdateDto.class));
    }

    @Test
    @DisplayName("대댓글 작성 성공 - 부모 댓글 존재")
    void addComment_Success_ReplyComment() {
        // given
        Long userId = 1L;
        Long bookId = 10L;
        Long parentId = 50L;

        Comment parentComment = CommentTestBuilder.aComment()
                .withCommentId(parentId)
                .withBookId(bookId)
                .withWriterId(2L)
                .build();

        CreateCommentRequest request = RequestDtoTestBuilder.commentReplyRequest(bookId, "Reply Comment", parentId);

        Comment savedReply = CommentTestBuilder.aComment()
                .withCommentId(100L)
                .withBookId(bookId)
                .withWriterId(userId)
                .withContent("Reply Comment")
                .build();

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(commentRepository.findById(parentId)).willReturn(Optional.of(parentComment));
        given(commentRepository.save(any(Comment.class))).willReturn(savedReply);

        // when
        Long commentId = reactionService.addComment(request);

        // then
        assertThat(commentId).isEqualTo(100L);
        verify(commentRepository, times(1)).findById(parentId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 작성 실패 - 부모 댓글 존재하지 않음")
    void addComment_Fail_ParentNotFound() {
        // given
        Long userId = 1L;
        Long bookId = 10L;
        Long parentId = 999L;

        CreateCommentRequest request = RequestDtoTestBuilder.commentReplyRequest(bookId, "Reply Comment", parentId);

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(commentRepository.findById(parentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reactionService.addComment(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("대댓글 작성 실패 - 다른 소설의 댓글에 대댓글 작성")
    void addComment_Fail_DifferentBook() {
        // given
        Long userId = 1L;
        Long bookId = 10L;
        Long parentId = 50L;

        Comment parentComment = CommentTestBuilder.aComment()
                .withCommentId(parentId)
                .withBookId(20L) // 다른 bookId
                .withWriterId(2L)
                .build();

        CreateCommentRequest request = RequestDtoTestBuilder.commentReplyRequest(bookId, "Reply Comment", parentId);

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(commentRepository.findById(parentId)).willReturn(Optional.of(parentComment));

        // when & then
        assertThatThrownBy(() -> reactionService.addComment(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("댓글 수정 성공 - 관리자")
    void modifyComment_Success_Admin() {
        // given
        Long adminId = 99L;
        Long commentId = 100L;
        Comment comment = CommentTestBuilder.aComment()
                .withCommentId(commentId)
                .withWriterId(1L)
                .withContent("Old Content")
                .build();

        UpdateCommentRequest request = RequestDtoTestBuilder.updateCommentRequest("Updated by Admin");

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(SecurityUtil.getCurrentUserId()).willReturn(adminId);
        given(SecurityUtil.isAdmin()).willReturn(false); // modifyComment은 관리자도 본인만 수정 가능

        // when & then
        assertThatThrownBy(() -> reactionService.modifyComment(commentId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_OWNER);
    }

    @Test
    @DisplayName("소설 투표 WebSocket 브로드캐스트 확인")
    void voteBook_BroadcastsVoteCount() {
        // given
        Long userId = 1L;
        Long bookId = 10L;
        VoteRequest request = RequestDtoTestBuilder.likeRequest(bookId);

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(bookVoteRepository.findByBookIdAndVoterId(bookId, userId)).willReturn(Optional.empty());
        given(bookVoteRepository.countByBookIdAndVoteType(bookId, VoteType.LIKE)).willReturn(5L);
        given(bookVoteRepository.countByBookIdAndVoteType(bookId, VoteType.DISLIKE)).willReturn(2L);

        // when
        reactionService.voteBook(request);

        // then
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/books/" + bookId + "/votes"),
                argThat((VoteUpdateDto dto) ->
                        dto.getTargetType().equals("BOOK") &&
                        dto.getTargetId().equals(bookId) &&
                        dto.getLikeCount() == 5L &&
                        dto.getDislikeCount() == 2L
                )
        );
    }

    @Test
    @DisplayName("문장 투표 WebSocket 브로드캐스트 확인")
    void voteSentence_BroadcastsVoteCount() {
        // given
        Long userId = 1L;
        Long sentenceId = 50L;
        Long bookId = 10L;
        VoteRequest request = RequestDtoTestBuilder.voteRequest(null, VoteType.DISLIKE);

        Book book = BookTestBuilder.aBook().withBookId(bookId).build();
        Sentence sentence = SentenceTestBuilder.aSentence()
                .withSentenceId(sentenceId)
                .withBook(book)
                .build();

        given(SecurityUtil.getCurrentUserId()).willReturn(userId);
        given(sentenceVoteRepository.findBySentenceIdAndVoterId(sentenceId, userId))
                .willReturn(Optional.empty());
        given(sentenceRepository.findById(sentenceId)).willReturn(Optional.of(sentence));
        given(sentenceVoteRepository.countBySentenceIdAndVoteType(sentenceId, VoteType.LIKE)).willReturn(3L);
        given(sentenceVoteRepository.countBySentenceIdAndVoteType(sentenceId, VoteType.DISLIKE)).willReturn(7L);

        // when
        reactionService.voteSentence(sentenceId, request);

        // then
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/books/" + bookId + "/votes"),
                argThat((VoteUpdateDto dto) ->
                        dto.getTargetType().equals("SENTENCE") &&
                        dto.getTargetId().equals(sentenceId) &&
                        dto.getLikeCount() == 3L &&
                        dto.getDislikeCount() == 7L
                )
        );
    }
}
