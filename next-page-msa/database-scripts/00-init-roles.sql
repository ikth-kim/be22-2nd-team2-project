-- ================================================
-- 00. Role Initialization Script
-- 실행: root 계정으로 실행
-- ================================================

-- 1. swcamp 계정 생성 (비밀번호: swcamp)
CREATE USER IF NOT EXISTS 'swcamp' @'%' IDENTIFIED BY 'swcamp';

CREATE USER IF NOT EXISTS 'swcamp' @'localhost' IDENTIFIED BY 'swcamp';

-- 2. 권한 부여
-- swcamp 계정이 DB를 생성하고 테이블을 관리할 수 있도록 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'swcamp' @'%';

GRANT ALL PRIVILEGES ON *.* TO 'swcamp' @'localhost';

FLUSH PRIVILEGES;

-- 확인
SELECT user, host FROM mysql.user WHERE user = 'swcamp';