-- ================================================
-- 01. Create Databases Script
-- 실행: swcamp 계정으로 실행 (권장) 또는 root
-- ================================================

-- 1. Member Service Database
CREATE DATABASE IF NOT EXISTS `next_page_member` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Story Service Database
CREATE DATABASE IF NOT EXISTS `next_page_story` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Reaction Service Database
CREATE DATABASE IF NOT EXISTS `next_page_reaction` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 생성 확인
SHOW DATABASES LIKE 'next_page_%';