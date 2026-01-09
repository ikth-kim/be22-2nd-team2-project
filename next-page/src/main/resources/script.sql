-- 1. 사용자 (Users)
CREATE TABLE `users` (
    `user_id` INT NOT NULL AUTO_INCREMENT,
    `user_email` VARCHAR(100) NOT NULL COMMENT '로그인 ID',
    `user_pw` VARCHAR(255) NOT NULL,
    `user_nicknm` VARCHAR(50) NOT NULL,
    `user_role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    `user_status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, DELETED',
    `left_at` DATETIME NULL COMMENT '탈퇴일시',
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_users_email` (`user_email`),
    UNIQUE KEY `uk_users_nicknm` (`user_nicknm`)
);

-- 2. 카테고리 (Categories)
CREATE TABLE `categories` (
    `category_id` VARCHAR(20) NOT NULL COMMENT 'PK: THRILLER, ROMANCE',
    `category_nm` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`category_id`)
);

-- 3. 소설 (Books)
CREATE TABLE `books` (
    `book_id` INT NOT NULL AUTO_INCREMENT,
    `writer_id` INT NOT NULL,
    `category_id` VARCHAR(20) NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'WRITING',
    `current_sequence` INT NOT NULL DEFAULT 1,
    `max_sequence` INT NOT NULL DEFAULT 20,
    `last_writer_user_id` INT NULL COMMENT '연속 작성 방지',
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    PRIMARY KEY (`book_id`)
);

-- 4. 문장 (Sentences)
CREATE TABLE `sentences` (
    `sentence_id` INT NOT NULL AUTO_INCREMENT,
    `book_id` INT NOT NULL,
    `writer_id` INT NOT NULL,
    `content` TEXT NOT NULL,
    `sequence_no` INT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`sentence_id`)
);

-- 5. 댓글 (Comments)
CREATE TABLE `comments` (
    `comment_id` INT NOT NULL AUTO_INCREMENT,
    `book_id` INT NOT NULL,
    `writer_id` INT NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME NULL,
    PRIMARY KEY (`comment_id`)
);

-- 6. 소설 투표 (Book Votes)
CREATE TABLE `book_votes` (
    `vote_id` INT NOT NULL AUTO_INCREMENT,
    `book_id` INT NOT NULL,
    `voter_id` INT NOT NULL,
    `vote_type` VARCHAR(10) NOT NULL COMMENT 'LIKE, DISLIKE',
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`vote_id`),
    UNIQUE KEY `uk_book_voter` (`book_id`, `voter_id`),
    CONSTRAINT `chk_book_vote_type` CHECK (
        `vote_type` IN ('LIKE', 'DISLIKE')
    )
);

-- 7. 문장 투표 (Sentence Votes)
CREATE TABLE `sentence_votes` (
    `vote_id` INT NOT NULL AUTO_INCREMENT,
    `sentence_id` INT NOT NULL,
    `voter_id` INT NOT NULL,
    `vote_type` VARCHAR(10) NOT NULL COMMENT 'LIKE, DISLIKE',
    `created_at` DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`vote_id`),
    UNIQUE KEY `uk_sentence_voter` (`sentence_id`, `voter_id`),
    CONSTRAINT `chk_sentence_vote_type` CHECK (
        `vote_type` IN ('LIKE', 'DISLIKE')
    )
);