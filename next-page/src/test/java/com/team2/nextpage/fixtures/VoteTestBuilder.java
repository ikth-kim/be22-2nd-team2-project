package com.team2.nextpage.fixtures;

import com.team2.nextpage.command.reaction.entity.BookVote;
import com.team2.nextpage.command.reaction.entity.SentenceVote;
import com.team2.nextpage.command.reaction.entity.VoteType;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Vote 엔티티 테스트 빌더
 * BookVote와 SentenceVote 생성을 간편하게 지원합니다.
 */
public class VoteTestBuilder {

    /**
     * BookVote 빌더
     */
    public static class BookVoteBuilder {
        private Long voteId = 1L;
        private Long bookId = 1L;
        private Long voterId = 1L;
        private VoteType voteType = VoteType.LIKE;

        public static BookVoteBuilder aBookVote() {
            return new BookVoteBuilder();
        }

        public BookVoteBuilder withVoteId(Long voteId) {
            this.voteId = voteId;
            return this;
        }

        public BookVoteBuilder withBookId(Long bookId) {
            this.bookId = bookId;
            return this;
        }

        public BookVoteBuilder withVoterId(Long voterId) {
            this.voterId = voterId;
            return this;
        }

        public BookVoteBuilder asLike() {
            this.voteType = VoteType.LIKE;
            return this;
        }

        public BookVoteBuilder asDislike() {
            this.voteType = VoteType.DISLIKE;
            return this;
        }

        public BookVote build() {
            BookVote vote = BookVote.builder()
                    .bookId(bookId)
                    .voterId(voterId)
                    .voteType(voteType)
                    .build();

            if (voteId != null) {
                ReflectionTestUtils.setField(vote, "voteId", voteId);
            }

            return vote;
        }
    }

    /**
     * SentenceVote 빌더
     */
    public static class SentenceVoteBuilder {
        private Long voteId = 1L;
        private Long sentenceId = 1L;
        private Long voterId = 1L;
        private VoteType voteType = VoteType.LIKE;

        public static SentenceVoteBuilder aSentenceVote() {
            return new SentenceVoteBuilder();
        }

        public SentenceVoteBuilder withVoteId(Long voteId) {
            this.voteId = voteId;
            return this;
        }

        public SentenceVoteBuilder withSentenceId(Long sentenceId) {
            this.sentenceId = sentenceId;
            return this;
        }

        public SentenceVoteBuilder withVoterId(Long voterId) {
            this.voterId = voterId;
            return this;
        }

        public SentenceVoteBuilder asLike() {
            this.voteType = VoteType.LIKE;
            return this;
        }

        public SentenceVoteBuilder asDislike() {
            this.voteType = VoteType.DISLIKE;
            return this;
        }

        public SentenceVote build() {
            SentenceVote vote = SentenceVote.builder()
                    .sentenceId(sentenceId)
                    .voterId(voterId)
                    .voteType(voteType)
                    .build();

            if (voteId != null) {
                ReflectionTestUtils.setField(vote, "voteId", voteId);
            }

            return vote;
        }
    }
}
