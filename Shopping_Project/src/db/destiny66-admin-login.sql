-- petshop 선택 후 전체를 한 번에 실행하세요.
USE petshop;

-- 비밀번호 1234 + 관리자 권한 한 번에 설정
UPDATE members
SET password = '$2a$10$smu7CPUgH3O4iHi6g63SxeI4fkp91FFgo7t6ra71sXxzyLwQzANpS',
    role = 'ADMIN'
WHERE username = 'destiny66';

SELECT username, role, LEFT(password, 30) AS password_prefix
FROM members
WHERE username = 'destiny66';
