-- ================================================
-- Story Service Database Schema
-- ================================================
-- 담당자: 정진호
-- 서비스: story-service (Port 8082)
-- 데이터베이스: next_page_story
-- ================================================

USE next_page_story;

-- 기존 테이블 삭제 (재실행 시 충돌 방지)
DROP TABLE IF EXISTS `sentences`;

DROP TABLE IF EXISTS `books`;

DROP TABLE IF EXISTS `categories`;

-- 1. 카테고리 테이블 (Categories)
-- 소설 장르 분류
CREATE TABLE `categories` (
    `category_id` VARCHAR(20) NOT NULL COMMENT 'PK: THRILLER, ROMANCE 등',
    `category_nm` VARCHAR(50) NOT NULL COMMENT '카테고리 한글명',
    PRIMARY KEY (`category_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '소설 카테고리 (Story Service)';

-- 2. 소설 테이블 (Books)
-- 릴레이 소설 방 (Book Aggregate Root)
CREATE TABLE `books` (
    `book_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '소설 ID (PK)',
    `writer_id` BIGINT NOT NULL COMMENT '작성자 ID (Member Service의 user_id)',
    `category_id` VARCHAR(20) NOT NULL COMMENT '카테고리 ID (FK)',
    `title` VARCHAR(200) NOT NULL COMMENT '소설 제목',
    `status` VARCHAR(20) NOT NULL DEFAULT 'WRITING' COMMENT '상태: WRITING, COMPLETED',
    `current_sequence` INT NOT NULL DEFAULT 1 COMMENT '현재 문장 순서 (1부터 시작)',
    `max_sequence` INT NOT NULL DEFAULT 20 COMMENT '최대 문장 개수 (도달 시 자동 완결)',
    `last_writer_user_id` BIGINT NULL COMMENT '마지막 작성자 ID (연속 작성 방지)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (`book_id`),
    INDEX `idx_writer_id` (`writer_id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at` DESC),
    INDEX `idx_category_status` (`category_id`, `status`),
    CONSTRAINT `fk_books_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '릴레이 소설 방 (Aggregate Root)';

-- 3. 문장 테이블 (Sentences)
-- Book Aggregate의 자식 엔티티
CREATE TABLE `sentences` (
    `sentence_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '문장 ID (PK)',
    `book_id` BIGINT NOT NULL COMMENT '소설 ID (FK)',
    `writer_id` BIGINT NOT NULL COMMENT '작성자 ID (Member Service의 user_id)',
    `content` TEXT NOT NULL COMMENT '문장 내용 (최소 10자, 최대 500자)',
    `sequence_no` INT NOT NULL COMMENT '문장 순서 (1부터 시작)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (`sentence_id`),
    UNIQUE KEY `uk_book_sequence` (`book_id`, `sequence_no`),
    INDEX `idx_writer_id` (`writer_id`),
    INDEX `idx_created_at` (`created_at` DESC),
    CONSTRAINT `fk_sentences_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '소설 문장 (Book Aggregate 소속)';

-- 초기 데이터: 카테고리
INSERT INTO
    categories (category_id, category_nm)
VALUES ('THRILLER', '스릴러'),
    ('ROMANCE', '로맨스'),
    ('FANTASY', '판타지'),
    ('SF', 'SF/공상과학'),
    ('MYSTERY', '미스터리'),
    ('DAILY', '일상/드라마');

-- 샘플 데이터: 소설 (개발/테스트용)
-- writer_id는 Member Service의 user_id와 매핑
INSERT INTO
    books (
        writer_id,
        category_id,
        title,
        status,
        current_sequence,
        max_sequence,
        last_writer_user_id,
        created_at
    )
VALUES (
        1,
        'THRILLER',
        '한밤중의 속삭임',
        'WRITING',
        1,
        20,
        NULL,
        NOW()
    ),
    (
        2,
        'FANTASY',
        '마법사의 귀환',
        'WRITING',
        1,
        15,
        NULL,
        NOW()
    ),
    (
        3,
        'ROMANCE',
        '첫눈에 반한 이야기',
        'COMPLETED',
        20,
        20,
        3,
        NOW()
    );

-- 샘플 데이터: 문장
INSERT INTO
    sentences (
        book_id,
        writer_id,
        content,
        sequence_no,
        created_at
    )
VALUES (
        1,
        1,
        '어두운 밤, 낯선 발소리가 들려왔다.',
        1,
        NOW()
    ),
    (
        2,
        2,
        '500년 만에 깨어난 대마법사는 놀라움을 금치 못했다.',
        1,
        NOW()
    ),
    (
        3,
        1,
        '그녀는 카페 문을 열고 들어왔다.',
        1,
        NOW()
    ),
    (
        3,
        2,
        '그 순간, 시간이 멈춘 것 같았다.',
        2,
        NOW()
    );

-- 스키마 확인
SHOW TABLES;

DESCRIBE books;

DESCRIBE sentences;

SELECT * FROM categories;