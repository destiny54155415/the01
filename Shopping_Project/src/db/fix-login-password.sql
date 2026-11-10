-- =========================================================
-- 로그인 안 될 때 MySQL 점검 / 비밀번호 수정
-- (Workbench에서 petshop DB 선택 후 실행)
-- =========================================================

-- 1) 계정과 비밀번호 저장 형태 확인
--    password 가 $2a$ 또는 $2b$ 로 시작하면 BCrypt 해시
--    그 외(예: destiny66, 1234) 는 평문
SELECT member_id, username, email, role,
       LEFT(password, 30) AS password_prefix,
       CHAR_LENGTH(password) AS password_len
FROM members
WHERE username IN ('destiny66', 'aa');

-- 2) destiny66 비밀번호를 1234 로 강제 설정 (BCrypt)
--    로그인: 아이디 destiny66 / 비밀번호 1234
UPDATE members
SET password = '$2a$10$smu7CPUgH3O4iHi6g63SxeI4fkp91FFgo7t6ra71sXxzyLwQzANpS'
WHERE username = 'destiny66';

-- 3) 그래도 안 되면 지우고 화면 회원가입(/signup)으로 다시 만들기
-- DELETE FROM members WHERE username = 'destiny66';

-- 4) aa 계정도 필요하면 비밀번호 aa 로 설정
-- UPDATE members
-- SET password = '$2a$10$CeI0jMgeYYk74F.je3mniONUGilCWpbVnRZe22aHBSoO5fUYpuf0C'
-- WHERE username = 'aa';

-- 5) 관리자 권한 (반드시 'ADMIN' — 'Admin' 금지, CHECK 제약)
UPDATE members
SET role = 'ADMIN'
WHERE username = 'destiny66';
