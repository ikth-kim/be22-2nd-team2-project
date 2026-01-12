package com.team2.nextpage.command.reaction.repository;

import com.team2.nextpage.command.reaction.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 댓글 Command Repository
 *
 * @author 정병진
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

}
