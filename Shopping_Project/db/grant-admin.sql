-- =========================================================
-- 관리자(ADMIN) 권한 부여
-- 주의: role 값은 반드시 'ADMIN' (대문자). 'Admin' 은 DB CHECK 제약에 걸립니다.
-- =========================================================

-- destiny66 을 관리자로
UPDATE members
SET role = 'ADMIN'
WHERE username = 'destiny66';

-- 확인
SELECT member_id, username, email, role
FROM members
WHERE username = 'destiny66';

-- 비밀번호도 1234 로 맞추려면 (선택)
-- UPDATE members
-- SET password = '$2a$10$smu7CPUgH3O4iHi6g63SxeI4fkp91FFgo7t6ra71sXxzyLwQzANpS'
-- WHERE username = 'destiny66';
