package com.team2.nextpage.query.reaction.service;

import com.team2.nextpage.query.reaction.dto.response.CommentDto;
import com.team2.nextpage.query.reaction.dto.response.CommentPageResponse;
import com.team2.nextpage.query.reaction.mapper.ReactionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReactionQueryServiceTest {

    @InjectMocks
    private ReactionQueryService reactionQueryService;

    @Mock
    private ReactionMapper reactionMapper;

    @Test
    @DisplayName("댓글 목록 조회 - 계층 구조 확인")
    void getComments_Success_Hierarchy() {
        // given
        Long bookId = 1L;

        CommentDto parent1 = new CommentDto();
        ReflectionTestUtils.setField(parent1, "commentId", 1L);
        ReflectionTestUtils.setField(parent1, "parentId", null);
        ReflectionTestUtils.setField(parent1, "content", "Parent 1");
        ReflectionTestUtils.setField(parent1, "children", new ArrayList<>());

        CommentDto child1_1 = new CommentDto();
        ReflectionTestUtils.setField(child1_1, "commentId", 2L);
        ReflectionTestUtils.setField(child1_1, "parentId", 1L);
        ReflectionTestUtils.setField(child1_1, "content", "Child 1-1");
        ReflectionTestUtils.setField(child1_1, "children", new ArrayList<>());

        CommentDto parent2 = new CommentDto();
        ReflectionTestUtils.setField(parent2, "commentId", 3L);
        ReflectionTestUtils.setField(parent2, "parentId", null);
        ReflectionTestUtils.setField(parent2, "content", "Parent 2");
        ReflectionTestUtils.setField(parent2, "children", new ArrayList<>());

        List<CommentDto> allComments = Arrays.asList(parent1, child1_1, parent2);

        given(reactionMapper.findCommentsByBookId(bookId)).willReturn(allComments);

        // when
        List<CommentDto> result = reactionQueryService.getComments(bookId);

        // then
        assertThat(result).hasSize(2); // parent1, parent2

        // Check hierarchy
        CommentDto resultParent1 = result.stream().filter(c -> c.getCommentId().equals(1L)).findFirst().orElseThrow();
        assertThat(resultParent1.getChildren()).hasSize(1);
        assertThat(resultParent1.getChildren().get(0).getCommentId()).isEqualTo(2L); // child1_1

        CommentDto resultParent2 = result.stream().filter(c -> c.getCommentId().equals(3L)).findFirst().orElseThrow();
        assertThat(resultParent2.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("댓글 목록 조회 - 고아 댓글 처리 (부모 없음)")
    void getComments_Orphan() {
        // given
        Long bookId = 1L;

        CommentDto orphan = new CommentDto();
        ReflectionTestUtils.setField(orphan, "commentId", 2L);
        ReflectionTestUtils.setField(orphan, "parentId", 999L); // Non-existent parent
        ReflectionTestUtils.setField(orphan, "content", "Orphan");
        ReflectionTestUtils.setField(orphan, "children", new ArrayList<>());

        List<CommentDto> allComments = Collections.singletonList(orphan);

        given(reactionMapper.findCommentsByBookId(bookId)).willReturn(allComments);

        // when
        List<CommentDto> result = reactionQueryService.getComments(bookId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCommentId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("사용자 작성 댓글 조회 성공")
    void getCommentsByUser_Success() {
        // given
        Long userId = 10L;
        int page = 0;
        int size = 10;

        CommentDto commentDto = new CommentDto();
        ReflectionTestUtils.setField(commentDto, "commentId", 100L);
        ReflectionTestUtils.setField(commentDto, "content", "My Comment");

        List<CommentDto> comments = Collections.singletonList(commentDto);

        given(reactionMapper.findCommentsByWriterId(userId, 0, 10)).willReturn(comments);
        given(reactionMapper.countCommentsByWriterId(userId)).willReturn(1L);

        // when
        CommentPageResponse response = reactionQueryService.getCommentsByUser(userId, page, size);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getContent()).isEqualTo("My Comment");
        assertThat(response.getTotalElements()).isEqualTo(1L);
    }
}
