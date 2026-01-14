-- ================================================
-- Reaction Service Database Schema
-- ================================================
-- 담당자: 정병진
-- 서비스: reaction-service (Port 8083)
-- 데이터베이스: next_page_reaction
-- ================================================

USE next_page_reaction;

-- 기존 테이블 삭제 (재실행 시 충돌 방지)
DROP TABLE IF EXISTS `sentence_votes`;

DROP TABLE IF EXISTS `book_votes`;

DROP TABLE IF EXISTS `comments`;

-- 1. 댓글 테이블 (Comments)
-- 소설에 대한 댓글 및 대댓글 (계층형 구조)
CREATE TABLE `comments` (
    `comment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '댓글 ID (PK)',
    `parent_id` BIGINT NULL COMMENT '부모 댓글 ID (대댓글인 경우)',
    `book_id` BIGINT NOT NULL COMMENT '소설 ID (Story Service의 book_id)',
    `writer_id` BIGINT NOT NULL COMMENT '작성자 ID (Member Service의 user_id)',
    `content` TEXT NOT NULL COMMENT '댓글 내용',
    `deleted_at` DATETIME NULL COMMENT 'Soft Delete 시각',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (`comment_id`),
    INDEX `idx_book_id` (`book_id`),
    INDEX `idx_writer_id` (`writer_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_book_created` (`book_id`, `created_at` DESC),
    INDEX `idx_deleted_at` (`deleted_at`),
    CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`comment_id`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '소설 댓글/대댓글 (Reaction Service)';

-- 2. 소설 투표 테이블 (Book Votes)
-- 소설에 대한 좋아요/싫어요 (1인 1투표 제약)
CREATE TABLE `book_votes` (
    `vote_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '투표 ID (PK)',
    `book_id` BIGINT NOT NULL COMMENT '소설 ID (Story Service의 book_id)',
    `voter_id` BIGINT NOT NULL COMMENT '투표자 ID (Member Service의 user_id)',
    `vote_type` VARCHAR(10) NOT NULL COMMENT '투표 타입: LIKE, DISLIKE',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '투표일시',
    PRIMARY KEY (`vote_id`),
    UNIQUE KEY `uk_book_voter` (`book_id`, `voter_id`),
    INDEX `idx_book_id` (`book_id`),
    INDEX `idx_voter_id` (`voter_id`),
    INDEX `idx_vote_type` (`vote_type`),
    CONSTRAINT `chk_book_vote_type` CHECK (
        `vote_type` IN ('LIKE', 'DISLIKE')
    )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '소설 투표 (1인 1투표)';

-- 3. 문장 투표 테이블 (Sentence Votes)
-- 문장에 대한 좋아요/싫어요 (1인 1투표 제약)
CREATE TABLE `sentence_votes` (
    `vote_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '투표 ID (PK)',
    `sentence_id` BIGINT NOT NULL COMMENT '문장 ID (Story Service의 sentence_id)',
    `voter_id` BIGINT NOT NULL COMMENT '투표자 ID (Member Service의 user_id)',
    `vote_type` VARCHAR(10) NOT NULL COMMENT '투표 타입: LIKE, DISLIKE',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '투표일시',
    PRIMARY KEY (`vote_id`),
    UNIQUE KEY `uk_sentence_voter` (`sentence_id`, `voter_id`),
    INDEX `idx_sentence_id` (`sentence_id`),
    INDEX `idx_voter_id` (`voter_id`),
    INDEX `idx_vote_type` (`vote_type`),
    CONSTRAINT `chk_sentence_vote_type` CHECK (
        `vote_type` IN ('LIKE', 'DISLIKE')
    )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '문장 투표 (1인 1투표)';

-- 샘플 데이터: 댓글 (개발/테스트용)
-- book_id, writer_id는 각각 Story Service, Member Service의 ID와 매핑
INSERT INTO
    comments (
        book_id,
        writer_id,
        content,
        parent_id,
        created_at
    )
VALUES (
        1,
        2,
        '분위기가 정말 좋네요! 다음 전개가 기대됩니다.',
        NULL,
        NOW()
    ),
    (
        1,
        3,
        '정말 소름 돋는 시작이네요.',
        NULL,
        NOW()
    ),
    (
        1,
        1,
        '감사합니다! 열심히 이어갈게요.',
        1,
        NOW()
    ),
    (
        2,
        3,
        '마법사 설정이 흥미롭습니다.',
        NULL,
        NOW()
    ),
    (
        3,
        2,
        '완결까지 정말 재미있게 읽었어요!',
        NULL,
        NOW()
    );

-- 샘플 데이터: 소설 투표
INSERT INTO
    book_votes (
        book_id,
        voter_id,
        vote_type,
        created_at
    )
VALUES (1, 2, 'LIKE', NOW()),
    (1, 3, 'LIKE', NOW()),
    (2, 1, 'LIKE', NOW()),
    (2, 3, 'LIKE', NOW()),
    (3, 1, 'LIKE', NOW()),
    (3, 2, 'LIKE', NOW());

-- 샘플 데이터: 문장 투표
INSERT INTO
    sentence_votes (
        sentence_id,
        voter_id,
        vote_type,
        created_at
    )
VALUES (1, 2, 'LIKE', NOW()),
    (1, 3, 'LIKE', NOW()),
    (2, 1, 'LIKE', NOW()),
    (3, 2, 'LIKE', NOW()),
    (4, 1, 'LIKE', NOW());

-- 스키마 확인
SHOW TABLES;

DESCRIBE comments;

DESCRIBE book_votes;

DESCRIBE sentence_votes;

-- 데이터 확인
SELECT COUNT(*) AS total_comments
FROM comments
WHERE
    deleted_at IS NULL;

SELECT COUNT(*) AS total_book_votes FROM book_votes;

SELECT COUNT(*) AS total_sentence_votes FROM sentence_votes;